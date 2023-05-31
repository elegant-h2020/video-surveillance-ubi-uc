package eu.ubitech.video.app.flink.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import eu.ubitech.video.app.flink.util.*;
//import org.apache.flink.configuration.ConfigConstants;
//import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.log4j.Logger;

/**
 * Class to consume incoming JSON messages from Kafka and process them using Flink Structured Streaming.
 */
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

        //Deserialising the data consumed from the topic "Video-stream-event" in kafka
        // using the properties file and JSonDeserializer
        FlinkKafkaConsumer010<VideoEventStringData> flinkConsumer = new FlinkKafkaConsumer010 <VideoEventStringData>(
                envMap.get(EnvKeys.KAFKA_TOPIC.toString())
                .toString(), new JsonDeserializer() , properties);

        FlinkKafkaProducer010<String> flinkProducer = new FlinkKafkaProducer010<String>
                (envMap.get(EnvKeys.KAFKA_TOPIC2.toString()).toString(),new SimpleStringSchema(), properties);


        //set up output directory
        final String processedImageDir = envMap.get(EnvKeys.PROCC_OUT_DIR.toString());
        logger.warn("Output directory for saving processed images is set to "+ processedImageDir);

        DataStreamSink<String> stream = env.addSource(flinkConsumer)
                .keyBy("cameraId")
                .map(new TransformFunction())
                .filter(new FaceDataFilter())
                .addSink(flinkProducer)
                ;


        if(execution_process.equals("FLINK")){
            env.execute();
        }
        //NES-ENABLEMENT
        else if(execution_process.equals("NES")) {
            logger.info("NES");
        }
        else{
            logger.error("EXECUTION_PROCESS is not set properly, EXECUTION_PROCESS="+execution_process);
        }
    }
}