package eu.ubitech.video.app.transcoding.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jdk.internal.net.http.common.Log;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Base64;

public class TranscodeFunction extends  RichMapFunction<VideoEventStringData, String>  {

    // Loading the OpenCV core library
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    private static final Logger logger = Logger.getLogger(TranscodeFunction.class);

    private int frameWidth;
    private int frameHeight;
    private int colorspace;
    private String outputDir;

    public TranscodeFunction(int colorspace, int frameWidth, int frameHeight, String outputDir) {
        this.colorspace = colorspace ;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.outputDir = outputDir ;
    }

    @Override
    public String map(VideoEventStringData videoEventStringData) throws Exception {
        //Get frame from image
        Mat data = getMat(videoEventStringData);
        Mat dataProcessed = changeFrameResolution(data,frameWidth,frameHeight);
        dataProcessed = changeFrameColorSpace(dataProcessed,colorspace);
        byte[] encodedImage = encodeData(dataProcessed);
        saveImage(dataProcessed,videoEventStringData,outputDir);

        Gson gson = new Gson();
        JsonObject obj = new JsonObject();
        //Json Object that will be send as message
        obj.addProperty("cameraId",videoEventStringData.getCameraId());
        obj.addProperty("timestamp", videoEventStringData.getTimestamp());
        obj.addProperty("rows", videoEventStringData.getRows());
        obj.addProperty("cols", videoEventStringData.getCols());
        obj.addProperty("type", videoEventStringData.getType());
        obj.addProperty("data", Base64.getEncoder().encodeToString(encodedImage));

        String json = gson.toJson(obj);


        return json;
    }

    /** send from collector width==640 , height==480 of original image **/


    /**
     * Changes resolution of frame
     * based on config values
     * @param  data  the mat-object(frame) to be proceeded
     * @param frameWidth
     * @param frameHeight
     * @return     the alternated-frame
     */
    public Mat changeFrameResolution(Mat data, int frameWidth, int frameHeight){
        Mat resizeimage = new Mat();
        logger.info("Resize frame at width " +frameWidth +"frame height " +frameHeight);
        Size sz = new Size(frameWidth, frameHeight);
        Imgproc.resize( data, resizeimage, sz );
        return  resizeimage;
    }

    /**
     * Change ColorSpace of data
     * @param data the mat-object(frame)
     *     to change its colorspace
     * @param colorspace transformation
     * (0->indicates no
     *  conversio,6=COLOR_BGR2GRAY,
     * 40=COLOR_BGR2HSV, 83=COLOR_RGB2YUV)
     * @return  the alternated frame
     */
    public Mat changeFrameColorSpace(Mat data, int colorspace){
        Mat dst = new Mat();
        logger.info("Change colorspace to " + colorspace + "\n");
        if (colorspace != 0) {
            //Converting From BGR to colorspace
            Imgproc.cvtColor(data, dst, 83);
            return dst;
        }
        else return data ;
    }


    /**
     *  Encodes Mat image to array of bytes
     * @param image
     * @return
     */
    public byte[] encodeData(Mat image){
        byte[] binaryImage = new byte[(int) (image.total() * image.channels())];
        image.get(0, 0, binaryImage);
        return binaryImage;
    }


    public static Mat getMat(VideoEventStringData ed) throws Exception {
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
