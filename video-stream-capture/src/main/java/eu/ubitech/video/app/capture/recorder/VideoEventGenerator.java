package eu.ubitech.video.app.capture.recorder;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Base64;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Class to convert Video Frame into byte array and generate JSON event using Kafka Producer.
 */
public class VideoEventGenerator implements Runnable {
    private static final Logger logger = Logger.getLogger(VideoEventGenerator.class);

    private final String cameraId;
    private final String url;
    private final Producer<String, String> producer;
    private final String topic;
    private final String capture_mode;
    private final String file_loc;

    public String getCameraId() {
        return cameraId;
    }

    public String getUrl() {
        return url;
    }

    public Producer<String, String> getProducer() {
        return producer;
    }

    public String getTopic() {
        return topic;
    }

    public String getCapture_mode() {
        return capture_mode;
    }

    public String getFile_loc() {
        return file_loc;
    }

    public static class VideoEventGenBuilder{

        private  String nestedCameraId;
        private  String nestedUri;
        private Producer<String,String> nestedProducer;
        private String nestedCaptureMode;
        private String nestedFile_loc;
        private String nestedTopic ;

        public VideoEventGenBuilder(Producer<String,String> producer, String topic){
            this.nestedProducer = producer;
            this.nestedTopic = topic ;
        }

        public VideoEventGenBuilder captureMode(String capture_mode){
            this.nestedCaptureMode = capture_mode ;
            return this ;
        }

        public VideoEventGenBuilder fileLoc(String file_loc){
            this.nestedFile_loc = file_loc ;
            return this ;
        }

        public VideoEventGenBuilder cameraId(String cameraID){
            this.nestedCameraId= cameraID ;
            return this ;
        }


        public VideoEventGenBuilder uri(String uri){
            this.nestedUri = uri  ;
            return this ;
        }

        public VideoEventGenerator build(){
            return  new VideoEventGenerator(this.nestedCameraId,
                    this.nestedUri,
                    this.nestedProducer,
                    this.nestedTopic,
                    this.nestedCaptureMode,
                    this.nestedFile_loc);
        }

    }

    //load OpenCV native lib
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private VideoEventGenerator(String cameraId,
                                String uriIp,
                                Producer<String, String> producer,
                                String kafka_topic,
                                String capture_mode,
                                String file_loc)
    {
        this.cameraId = cameraId;
        this.url = uriIp;
        this.producer = producer;
        this.topic = kafka_topic;
        this.file_loc = file_loc;
        this.capture_mode = capture_mode ;
    }

    /**
     * The driver function of each thread
     */
    @Override
    public void run() {
        logger.info("Processing cameraId "+cameraId+" with url "+url);
        try {
            generateEvent(cameraId,url,producer,topic,capture_mode,file_loc);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Generatos json event for every captured frame
     * @param cameraId name of host
     * @param url
     * @param producer Kafka producer to send data to
     * @param topic Kafka topic
     * @param capture_mode capture mode is set
     *                     to "REAL" if input is from
     *                     camera
     * @param file_loc The location of file e.g /tmp/sample-video
     *                      /business.mp4, to capture frame from
     *
     * @throws Exception
     */
    private void generateEvent(String cameraId, String url, Producer<String,
            String> producer, String topic,
                               String capture_mode,
                               String file_loc) throws Exception

    {
        logger.info("Capture Mode is set to : "+ capture_mode);
        VideoCapture camera = null;
        if (capture_mode.equals("REAL")) {
            //Open default camera
            camera = new VideoCapture(0);
            //check camera working
            logger.info("Is Camera working:" + camera.isOpened());
            if (!camera.isOpened()) {
                Thread.sleep(5000);
                if (!camera.isOpened()) {
                    throw new Exception("Error opening cameraId "+cameraId+". " +
                            "Set correct file path or url " +
                            "in camera.url key of property file.");
                }
            }
        }
        //Capture frames from file_loc
        else camera = new VideoCapture(file_loc);
        logger.info("File location to read video from is set to : " + file_loc);
        Mat mat = new Mat();
        Gson gson = new Gson();
        int frameCount = 0;

        //Start capturing
        while (camera.read(mat)) {
            //resize image before sending
            //This  can affect the similarity process
            Imgproc.resize(mat, mat, new Size(640, 480), 0, 0, Imgproc.INTER_CUBIC);
            int cols = mat.cols();
            int rows = mat.rows();
            int type = mat.type();
            byte[] data = new byte[(int) (mat.total() * mat.channels())];
            mat.get(0, 0, data);
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            JsonObject obj = new JsonObject();
            //Json Object that will be send as message
            obj.addProperty("cameraId",cameraId);
            obj.addProperty("timestamp", timestamp);
            obj.addProperty("rows", rows);
            obj.addProperty("cols", cols);
            obj.addProperty("type", type);
            obj.addProperty("data", Base64.getEncoder().encodeToString(data));
            String json = gson.toJson(obj);

            //Send to Kafka Producer
            producer.send(new ProducerRecord<String, String>(topic,cameraId, json),new EventGeneratorCallback(cameraId));
            logger.info("Generated events for cameraId=" + cameraId + " frameid=" + (++frameCount) + " timestamp="+timestamp);
        }
        camera.release();
        mat.release();
    }

    private class EventGeneratorCallback implements Callback {
        private String camId;
        public EventGeneratorCallback(String camId) {
            super();
            this.camId = camId;
        }

        @Override
        public void onCompletion(RecordMetadata rm, Exception e) {
            if (rm != null) {
                logger.info("cameraId="+ camId + " partition=" + rm.partition());
            }
            if (e != null) {
                e.printStackTrace();
            }
        }
    }

}
