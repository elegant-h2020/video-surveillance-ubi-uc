package eu.ubitech.video.app.similarity.processor;

import eu.ubitech.video.app.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import stream.nebula.exceptions.RESTException;
import stream.nebula.operators.sinks.KafkaSink;
import stream.nebula.runtime.NebulaStreamRuntime;
import stream.nebula.runtime.Query;
import stream.nebula.operators.sinks.FileSink;
import stream.nebula.udf.MapFunction;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static stream.nebula.expression.Expressions.attribute;

/**
 *  Similarity-Processor module responsible for
 *  feeding ML model with processed images
 *  and collecting similarities results
 */
public class SimilarityProcessor {
    private static final Logger log = Logger.getLogger(SimilarityProcessor.class);
    private static  final FaceNetSmallV2Model faceNetSmallV2Model = new FaceNetSmallV2Model();
    public  static FaceSimilarity faceSimilarity = new FaceSimilarity();


    //Load the OpenCV library
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**Connect to Kafka,
     * collect messages produced by video-stream-processor-module.
     * Process them via Flink-Map operations
     * Send output-results to Kafka
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //Add Flink environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //Flink parameters reading
        Properties properties = new Properties();

        //Get env variables
        HashMap<String, String> envHashMap = new HashMap<String, String>(System.getenv());
        EnvReader envReader = new EnvReader(envHashMap);

        // Get all the env variables
        Map<String,String> envMap = envReader.getEnvMap();

        properties.put(KafkaPropertiesNames.BOOTSTRAP_SERVERS.toString(),envMap.get("KAFKA_BOOTSTRAP_SERVERS").toString());
        properties.put(KafkaPropertiesNames.ACKS.toString(),envMap.get("ACKS").toString());
        properties.put(KafkaPropertiesNames.RETRIES.toString(),envMap.get("RETRIES").toString());
        properties.put(KafkaPropertiesNames.BATCH_SIZE.toString(),envMap.get("BATCH_SIZE").toString());
        properties.put(KafkaPropertiesNames.LINGER_MS.toString(),envMap.get("LINGER_MS").toString());
        properties.put(KafkaPropertiesNames.MAX_REQUEST_SIZE.toString(),envMap.get("MAX_REQUEST_SIZE").toString());
        properties.put(KafkaPropertiesNames.COMPRESSION_TYPE.toString(),envMap.get("COMPRESSION_TYPE").toString());
        properties.put(KafkaPropertiesNames.KEY_DESERIALIZER.toString(),StringDeserializer.class.getName());
        properties.put(KafkaPropertiesNames.VALUE_DESERIALIZER.toString(),VideoEventStringDeserializer.class.getName());
        properties.put("group.id","ubi");

        //Get parameters
        String execution_process = envMap.get("EXECUTION_PROCESS");

        //Load Model once-here
        faceSimilarity.loadModel();
        addFacesFromImages();

        //Deserializing the data consumed from the topic "Video-stream-event" in kafka using the properties file and JSonDeserializer
        FlinkKafkaConsumer010<VideoEventStringProcessed> flinkConsumer = new FlinkKafkaConsumer010 <VideoEventStringProcessed>(
                envMap.get(EnvKeys.KAFKA_TOPIC.toString()),
                new VideoEventStringDeserializer(),
                properties);

        //Initializing the Flink-Kafka-producer:
        //that output-data are sent to.
        FlinkKafkaProducer010<String> flinkProducer = new FlinkKafkaProducer010<String>(
                envMap.get(EnvKeys.KAFKA_TOPIC2.toString()),
                new SimpleStringSchema(),
                properties);


        //Flink operations
        DataStreamSink<String> stream =
                //collect data from Kafka
                env.addSource(flinkConsumer)
                        //Use UDF-Similarity-Process
                        .map(new SimilarityProcess())
                        //Send Data to Kafka
                        .addSink(flinkProducer);



        if(execution_process.equals("FLINK")){
            env.execute();
        }
        //NES-ENABLEMENT
        else if(execution_process.equals("NES")) {
            log.info("NES");
//            NebulaStreamRuntime ner = NebulaStreamRuntime.getRuntime();
//            ner.getConfig().setHost("212.101.173.11").setPort("8081");
//            System.out.println(" *** connected:\n");
//            nebulaStreamKafkaProcessing(ner);
        }
        else{
            log.error("EXECUTION_PROCESS is not set properly, EXECUTION_PROCESS="+execution_process);
        }
    }
//    public static void nebulaStreamKafkaProcessing(NebulaStreamRuntime ner) throws RESTException, IOException {
//
//        Query worker = ner.readFromSource("");
//        worker.project(attribute("data"), attribute("cameraId"), attribute("timestamp"),
//                        attribute("rows"), attribute("cols"), attribute("type"))
//                .map(new SimilarityProcessMapper());
//        worker.sink(new FileSink("/output_kafka_FLINK_NES_query.csv", "CSV_FORMAT", true));
//        int queryId = ner.executeQuery(worker, "BottomUp");
//
//        System.out.println("Query + " + queryId);
//    }
//
//    static class InputEvent {
//        String data;
//        String cameraId;
//        String timestamp;
//        int rows;
//        int cols;
//        int type;
//    }
//
//
//    static class OutputEvent {
//        String data;
//        String cameraId;
//        String timestamp;
//        int rows;
//        int cols;
//        int type;
//    }
//
//
//    static class SimilarityProcessMapper implements MapFunction<InputEvent, OutputEvent> {
//        public OutputEvent map(final InputEvent inputEvent) {
//            OutputEvent outputEvent =  new OutputEvent();
//            // Mapping logic here
//            return outputEvent;
//        }
//    }
//
//    private static void readFromKafka() throws  Exception {
//        NebulaStreamRuntime ner = NebulaStreamRuntime.getRuntime();
//        ner.getConfig().setHost("212.101.173.11").setPort("8081");
//        System.out.println(" *** connected:\n");
//
//        Query w1 = ner.readFromSource("kafka_source"); //here should it be replaced with kafka_stream? TODO
//
//        w1.sink(new KafkaSink("example-output", "212.101.173.100:29092", 100));
//        int queryId = ner.executeQuery(w1, "BottomUp");
//
//        System.out.println("Query + " + queryId);
//        System.out.println(" *** \n");
//    }


    // Save image file
    public static void saveImage(Mat mat, VideoEventStringProcessed ed) {
        long currrentUnixTimestamp = System.currentTimeMillis() / 1000;
        String outputDirName = EnvKeys.OUTPUT_DIR.toString();
        String outputDir = System.getenv(outputDirName);
        if(outputDir != null){
            // null-check statement
            log.info("Gonna save image");
        }else {
            log.info("Need to set OUTPUT_DIR env variable");
            System.exit(-1);
        }
        String imagePath = outputDir + ed.getCameraId() + "-T-" + String.valueOf(currrentUnixTimestamp) + ".png";
        log.warn("Saving images to " + imagePath + "\n");
        boolean result = Imgcodecs.imwrite(imagePath, mat);
        if (!result) {
            log.error("Couldn't save images to path " + outputDir
                    + ".Please check if this path exists. This is configured in processed.output.dir key of property file.");
        }

    }


    private static Mat getMat(VideoEventStringProcessed ed) throws Exception {
        /*
         * The mat object needs to match
         * the rows and cols of indarray of network
         */
        Mat mat = new Mat(ed.getRows(), ed.getCols(), ed.getType());
        mat.put(0, 0, Base64.getDecoder().decode(ed.getData()));
        return mat;
    }

    public static void addFacesFromImages() throws IOException {
        String base_path = System.getenv(EnvKeys.BASE_PATH.toString()).toString();
        File[] files = new File(base_path).listFiles();
        for (File file : Objects.requireNonNull(files)) {
            File[] images = file.listFiles();
            addInitialMember(Objects.requireNonNull(images)[0]);
        }
    }


    public static void addInitialMember(File imageFile) throws IOException {
        addInitialMember(imageFile, null);
    }

    public static void addInitialMember(File imageFile, String name) throws IOException {
        String title = name ;
        if (StringUtils.isBlank(name)) {
            title = imageFile.getName().replaceAll("_", "")
                    .replace(".jpg", "")
                    .replace(".png", "")
                    .replaceAll("[0-9]", "");
        }
        faceSimilarity.registerNewMember(title , imageFile.getAbsolutePath());
        log.info("Register new member " + title + "\n");
    }

}
