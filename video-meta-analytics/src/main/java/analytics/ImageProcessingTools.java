package analytics;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class ImageProcessingTools {

    public static void main(String[] args) {

        // Load the OpenCV library - Configure accordingly
        // nes-elegant-image includes opencv so the following line is kept for dev usage
        System.load("/opt/opencv-4.5.4/build/lib/libopencv_java454.so");

    }

    // Reads an image from argument path, encodes it to Base64 string,
    // writes it to CSV file, and stores it in the output path
    private static void encodeImageToCSV(String imagePath, String outputCSVPath) {
        Mat inputImage = Imgcodecs.imread(imagePath);

        if (inputImage.empty()) {
            System.out.println("Error: Couldn't read the image.");
            return;
        }

        // Encode the image to a Base64 string
        String encodedImage = encodeImageToString(inputImage);

        // Store the encoded image in a CSV column
        try (FileWriter writer = new FileWriter(outputCSVPath)) {
            writer.write("frame\n");  // CSV header
            writer.write(encodedImage + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processImagesFromCSV(String inputCSV, String outputCSV) {
        List<String> encodedImages = readCSV(inputCSV);

        // Process each input image
        try (FileWriter writer = new FileWriter(outputCSV)) {
            writer.write("frame\n");  // CSV header
            for (String encodedImage : encodedImages) {
                String encodedProcessedImage = encodedStringImageToGrayScaleEncodedStringImage(encodedImage);
//                // Write the processed image to the output CSV
//                writer.write(encodedProcessedImage + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Gets a string encoded image, decodes it, converts to graycale, then string encodes it again
    public static String encodedStringImageToGrayScaleEncodedStringImage(String encodedImage) {
        Mat inputImage = decodeStringToImage(encodedImage);

        Mat processedImage = new Mat();
        Imgproc.cvtColor(inputImage, processedImage, Imgproc.COLOR_BGR2GRAY);

        // Encode the processed image to string
        String encodedProcessedImage = encodeImageToString(processedImage);

        // Release resources
        inputImage.release();
        processedImage.release();

        return encodedProcessedImage;
    }

    // OpenCV Image to String Base64
    private static String encodeImageToString(Mat image) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        return Base64.getEncoder().encodeToString(byteArray);
    }

    // Base64 String Image to OpenCV Image
    private static Mat decodeStringToImage(String encodedImage) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedImage);
        return Imgcodecs.imdecode(new MatOfByte(decodedBytes), Imgcodecs.IMREAD_COLOR);
    }

    // Read CSV and return a list of String with the encoded Base64 Images
    private static List<String> readCSV(String csvPath) {
        List<String> encodedImages = new java.util.ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            // In order to Skip the header
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                encodedImages.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encodedImages;
    }
}
