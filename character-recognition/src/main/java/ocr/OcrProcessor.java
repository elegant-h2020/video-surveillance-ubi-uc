package ocr;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.*;

public class OcrProcessor {

    public static void main(String[] args) {

        // Load the OpenCV library - Configure accordingly
        // nes-elegant-image includes opencv so the following line is kept for dev usage
        System.load("/opt/opencv-4.5.4/build/lib/libopencv_java454.so");

        String modelPath = "path/to/handwriting.model";
        String imagePath = "path/to//hello_world.png";

        // Load the handwriting OCR model
        System.out.println("[INFO] Loading handwriting OCR model...");
        Model model;
        try {
            model = KerasModelImport.importKerasModelAndWeights(modelPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidKerasConfigurationException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedKerasConfigurationException e) {
            throw new RuntimeException(e);
        }
        OcrProcessor ocrProcessor = new OcrProcessor();
        ocrProcessor.processImage(imagePath, (ComputationGraph) model);

    }

    private static Mat decodeStringToImage(String encodedImage) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedImage);
        return Imgcodecs.imdecode(new MatOfByte(decodedBytes), Imgcodecs.IMREAD_COLOR);
    }

    private void processImage(String imagePath, ComputationGraph model) {
        Mat inputMatImage = Imgcodecs.imread(imagePath);

        // Convert the image to grayscale
        Mat grayscaleImage = new Mat();
        Imgproc.cvtColor(inputMatImage, grayscaleImage, Imgproc.COLOR_BGR2GRAY);

        // Apply Gaussian blur
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayscaleImage, blurredImage, new Size(5, 5), 0);

        Mat edgedImage = new Mat();
        Imgproc.Canny(blurredImage, edgedImage, 30, 150);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edgedImage.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Sort contours from left to right
        Collections.sort(contours, (c1, c2) -> {
            double x1 = Imgproc.boundingRect(c1).tl().x;
            double x2 = Imgproc.boundingRect(c2).tl().x;
            return Double.compare(x1, x2);
        });

        List<CustomPair<INDArray, Rect>> chars = new ArrayList<>();

        for (MatOfPoint contour : contours) {
            // Perform operations on each contour

            // Extract bounding rectangle for each contour
            // For example, you can get the coordinates and dimensions of the bounding rectangle
            Rect boundingRect = Imgproc.boundingRect(contour);
            double x = boundingRect.tl().x;
            double y = boundingRect.tl().y;
            double w = boundingRect.width;
            double h = boundingRect.height;

            if ((w >= 5 && w <= 150) && (h >= 15 && h <= 120)) {
                Mat roiImage = new Mat(grayscaleImage, new Rect((int) x, (int) y, (int) w, (int) h));
                Mat threshImage = new Mat();
                Imgproc.threshold(roiImage, threshImage, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
                int tH = threshImage.rows();
                int tW = threshImage.cols();

                // Resize the image based on the condition
                if (tW > tH) {
                    Imgproc.resize(threshImage, threshImage, new Size(32, (32.0 / tW) * tH));
                } else {
                    Imgproc.resize(threshImage, threshImage, new Size((32.0 / tH) * tW, 32));
                }

                int dX = (int) Math.max(0, (32 - tW) / 2.0);
                int dY = (int) Math.max(0, (32 - tH) / 2.0);

                // Pad the image to force 32x32 dimensions
                Mat paddedImage = new Mat();
                Core.copyMakeBorder(threshImage, paddedImage, dY, dY, dX, dX, Core.BORDER_CONSTANT, new Scalar(0, 0, 0));
                Mat resizedImage = new Mat();
                Imgproc.resize(paddedImage, resizedImage, new Size(32, 32));

                Mat paddedFloatImage = new Mat();
                resizedImage.convertTo(paddedFloatImage, CvType.CV_32F, 1.0 / 255.0);

                // Expand dimensions to match the expected input format
                Mat expandedImage = new Mat();
                Core.merge(new java.util.ArrayList<>(java.util.Collections.singletonList(paddedFloatImage)), expandedImage);                INDArray expandedImageIND = imageToINDArray(expandedImage);
                chars.add(new CustomPair<>(expandedImageIND, new Rect((int) x, (int) y, (int) w, (int) h)));
            }
        }

        List<Rect> boxes = new ArrayList<>();
        List<INDArray> charsMat = new ArrayList<>();
        for (CustomPair<INDArray, Rect> pair : chars) {
            boxes.add(pair.getValue());
            charsMat.add(pair.getKey());
        }

        // code to be completed here
        // Start making predictions with the model
        INDArray charArrayNDArray = Nd4j.vstack(charsMat);
        INDArray[] preds = model.output(charArrayNDArray);

        // Make predictions with the model

        System.out.println("G");

    }

    private static INDArray imageToINDArray(Mat image) {
        MatOfFloat matOfFloat = new MatOfFloat();
        image.convertTo(matOfFloat, CvType.CV_32F);
        float[] floatArray = new float[(int) (matOfFloat.total() * matOfFloat.channels())];
        matOfFloat.get(0, 0, floatArray);
        return Nd4j.create(floatArray).reshape(1, 32, 32, 1); //1 32 32 1
    }
    public class CustomPair<K, V> {
        private final K key;
        private final V value;

        public CustomPair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
