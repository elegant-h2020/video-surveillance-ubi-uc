package eu.ubitech.video.app.flink.processor;

import eu.ubitech.video.app.flink.util.EnvKeys;
import eu.ubitech.video.app.flink.util.EnvReader;
import eu.ubitech.video.app.flink.util.VideoEventStringData;
import eu.ubitech.video.app.flink.util.VideoEventStringProcessed;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.*;

public class VideoFaceDetection {

    private static final Logger logger = Logger.getLogger(VideoFaceDetection.class);

    // Loading the OpenCV core library
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Method to detect faces from batch of frames
     *
     * @param videoEventStringData data to be processed
     * @param outputDir directory to save image files
     * @return processed data
     * @throws Exception
     */
    public static VideoEventStringProcessed detectFace(VideoEventStringData videoEventStringData, String outputDir, String frameDir) throws Exception {

        Mat frame = getMat(videoEventStringData);

        //Get env variables
        HashMap<String, String> envHashMap = new HashMap<String, String>(System.getenv());
        EnvReader envReader = new EnvReader(envHashMap);
        // Get all the env variables
        Map<String,String> envMap = envReader.getEnvMap();

        String env_mode = envMap.get(EnvKeys.MODE.toString()) ;

        String xmlfile= envMap.get(EnvKeys.XML_FILE.toString());

        CascadeClassifier classifier = new CascadeClassifier(xmlfile);

        // Detecting the face in the snap
        MatOfRect faceDetections = new MatOfRect();
        classifier.detectMultiScale(frame, faceDetections);

        int numberofDetectedfaces = faceDetections.toArray().length;
        //Collect points
        List<Point> lista =  new LinkedList<>();

        if (numberofDetectedfaces >= 1 ) {
            // Drawing boxes
            for (Rect rect : faceDetections.toArray()) {
                Imgproc.rectangle(
                        frame,                                               // where to draw the box
                        new Point(rect.x, rect.y),                            // bottom left
                        new Point(rect.x + rect.width, rect.y + rect.height), // top right
                        new Scalar(0, 0, 255),
                        1                                                   // RGB colour
                );
                //Collect save Points
                lista.add(new Point(rect.x, rect.y));
                lista.add(new Point(rect.x + rect.width, rect.y + rect.height));
            }
            logger.info("Env mode is = "+env_mode);
            if(env_mode.equals("DEBUG")) {
                saveImage(frame, videoEventStringData, outputDir);
            }

        }

        if(env_mode.equals("DEBUG") ) {
            saveImage(frame, videoEventStringData, frameDir);
        }


        VideoEventStringProcessed videoEventStringProcessed = new VideoEventStringProcessed(
                videoEventStringData.getCameraId(),
                videoEventStringData.getTimestamp(),
                videoEventStringData.getRows(),
                videoEventStringData.getCols(),
                videoEventStringData.getType(),
                videoEventStringData.getData(),
                lista
        );
        logger.info("Video event was transformed");
        return videoEventStringProcessed;
    }

    // Get Mat from byte[]
    private static Mat getMat(VideoEventStringData ed) throws Exception {
        Mat mat = new Mat(ed.getRows(), ed.getCols(), ed.getType());
        mat.put(0, 0, Base64.getDecoder().decode(ed.getData()));
        return mat;
    }

    // Save image file
    private static void saveImage(Mat mat, VideoEventStringData ed, String outputDir) {
        long currrentUnixTimestamp = System.currentTimeMillis() / 1000;
        String imagePath = outputDir + ed.getCameraId() + "-T-" + String.valueOf(currrentUnixTimestamp) + ".png";
        logger.warn("Saving images to " + imagePath);
        boolean result = Imgcodecs.imwrite(imagePath, mat);
        if (!result) {
            logger.error("Couldn't save images to path " + outputDir
                    + ".Please check if this path exists. This is configured in processed.output.dir key of property file.");
        }
    }


}