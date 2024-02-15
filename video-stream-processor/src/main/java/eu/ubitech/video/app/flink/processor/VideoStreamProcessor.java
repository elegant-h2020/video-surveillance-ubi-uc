package eu.ubitech.video.app.flink.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import eu.ubitech.video.app.flink.util.*;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.log4j.Logger;
import stream.nebula.exceptions.RESTException;
import stream.nebula.operators.sinks.FileSink;
import stream.nebula.runtime.NebulaStreamRuntime;
import stream.nebula.runtime.Query;
import stream.nebula.udf.MapFunction;

import static stream.nebula.expression.Expressions.attribute;

/**
 * Class to consume incoming JSON messages from Kafka and process
 * them using Flink and OpenCV.
 **/
public class VideoStreamProcessor {
    private static final Logger logger = Logger.getLogger(VideoStreamProcessor.class);

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //Get env variables
        HashMap<String, String> envHashMap = new HashMap<String, String>(System.getenv());
        EnvReader envReader = new EnvReader(envHashMap);

        // Get all the env variables
        Map<String,String> envMap = envReader.getEnvMap();

        //Get Parameter
        String execution_process = envMap.get("EXECUTION_PROCESS");

        //Get Thread Parallelism in Flink
        String th_parallelism = envMap.get("FLINK_PARALLELISM");
        env.setParallelism(3);

        //Flink parameters reading
        Properties properties = new Properties();

        properties.put(KafkaPropertiesNames.BOOTSTRAP_SERVERS.toString(),envMap.get("BOOTSTRAP_SERVERS").toString());
        properties.put(KafkaPropertiesNames.GROUP_ID.toString(),envMap.get("GROUP_ID").toString());
        properties.put(KafkaPropertiesNames.ACKS.toString(),envMap.get("ACKS").toString());
        properties.put(KafkaPropertiesNames.RETRIES.toString(),envMap.get("RETRIES").toString());
        properties.put(KafkaPropertiesNames.BATCH_SIZE.toString(),envMap.get("BATCH_SIZE").toString());
        properties.put(KafkaPropertiesNames.LINGER_MS.toString(),envMap.get("LINGER_MS").toString());
        properties.put(KafkaPropertiesNames.MAX_REQUEST_SIZE.toString(),envMap.get("MAX_REQUEST_SIZE").toString());
        properties.put(KafkaPropertiesNames.COMPRESSION_TYPE.toString(),envMap.get("COMPRESSION_TYPE").toString());

        logger.info("Captured Frames need to contain a face in order to get ouput");

//        //Deserializing the data consumed from the topic "Video-stream-event" in Kafka
//        // using the properties file and JSonDeserializer
//        FlinkKafkaConsumer010<VideoEventStringData> flinkConsumer = new FlinkKafkaConsumer010 <VideoEventStringData>(
//                envMap.get(EnvKeys.KAFKA_TOPIC.toString())
//                .toString(), new JsonDeserializer() , properties);
//
//        //Send Generated JSON to output Kafka_Topic
//        FlinkKafkaProducer010<String> flinkProducer = new FlinkKafkaProducer010<String>
//                (envMap.get(EnvKeys.KAFKA_TOPIC2.toString()).toString(),new SimpleStringSchema(), properties);
//
//
//        //Set up output directory
//        final String processedImageDir = envMap.get(EnvKeys.PROCC_OUT_DIR.toString());
//        logger.warn("Output directory for saving processed images is set to "+ processedImageDir);
//
//
//        //Processing via Flink
//        DataStreamSink<String> stream = env.addSource(flinkConsumer)
//                //keyBy(cameraID)
//                .keyBy("cameraId")
//                //The main proccessing part
//                .map(new TransformFunction())
//                //Filter frames with no faces
//                .filter(new FaceDataFilter())
//                //Send to Kafka
//                .addSink(flinkProducer)
//                ;
        //Execute with Filnk
        if(execution_process.equals("FLINK")){
            env.execute();
        }
        //NES-ENABLEMENT
        else if(execution_process.equals("NES")) {
            logger.info("NES");
            NebulaStreamRuntime ner = NebulaStreamRuntime.getRuntime();
            ner.getConfig().setHost("212.101.173.11").setPort("8081");
            System.out.println(" *** connected:\n");
            nebulaStreamKafkaProcessing(ner);
        }
        else{
            logger.error("EXECUTION_PROCESS is not set properly, EXECUTION_PROCESS="+execution_process);
        }
    }
    public static void nebulaStreamKafkaProcessing(NebulaStreamRuntime ner) throws RESTException, IOException {

        Query worker = ner.readFromSource("kafka_source_event");
        worker.project(attribute("data"), attribute("cameraId"), attribute("timestamp"),
                        attribute("rows"), attribute("cols"), attribute("type"))
                .map(new FaceDetectionMapper());
        worker.sink(new FileSink("/output_kafka_DETECTION_NES_query.csv", "CSV_FORMAT", true));
        int queryId = ner.executeQuery(worker, "BottomUp");

        System.out.println("Query + " + queryId);
    }

    static class InputEvent {
        String data;
        String cameraId;
        String timestamp;
        int rows;
        int cols;
        int type;
    }


    static class OutputEvent  {
        String data;
        String cameraId;
        String timestamp;
        int rows;
        int cols;
        int type;
    }


    static class FaceDetectionMapper implements MapFunction<InputEvent, OutputEvent> {
        public OutputEvent map(final InputEvent inputEvent) {
            OutputEvent outputEvent =  new OutputEvent();
            HashMap<String, String> envHashMap = new HashMap<String, String>(System.getenv());

            final String processedImageDir = envHashMap.get(EnvKeys.PROCC_OUT_DIR.toString()).toString();
            final String frameImageDir = envHashMap.get(EnvKeys.FRAM_OUT_DIR.toString()).toString();

            //Detect-Face function
            try {
                VideoEventStringData inputV = new VideoEventStringData(inputEvent.cameraId, inputEvent.timestamp, inputEvent.rows, inputEvent.cols, inputEvent.type, inputEvent.data);
                VideoEventStringProcessed videoEventStringProcessed =  VideoFaceDetection.detectFace(inputV,processedImageDir,frameImageDir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return outputEvent;
        }
    }
}