package eu.ubitech.video.app.transcoding.driver;


import eu.ubitech.video.app.transcoding.util.*;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.log4j.Logger;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class VideoTranscodingDriver {

    private static final Logger logger = Logger.getLogger(VideoTranscodingDriver.class);

    public static void main(String[] args) throws Exception{


        //Read input from file
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


        //Set frame Resolution
        int frameWidth =Integer.valueOf(envMap.get(EnvKeys.FRAME_WIDTH.toString()));
        int frameHeight = Integer.valueOf(envMap.get(EnvKeys.FRAME_HEIGHT.toString()));

        logger.info("frameWidth to resize is set to "+ frameWidth+"\n");
        logger.info("frameHeight to resize  is set to "+ frameHeight+"\n");

        //Set frame colorspace
        int colorspace;
        try {
            colorspace = Integer.valueOf(envMap.get(EnvKeys.COLORSPACE.toString()));
        } catch (NumberFormatException e){
            colorspace = 0;
        }
        logger.info("colorspace to alternate is set to "+ colorspace+"\n");



        //Deserialising the data consumed from the topic "Video-stream-event" in kafka using the properties file and JSonDeserializer
        FlinkKafkaConsumer010<VideoEventStringData> flinkConsumer = new FlinkKafkaConsumer010<VideoEventStringData>(envMap.get(EnvKeys.KAFKA_TOPIC.toString()).toString()
                , new JsonDeserializer(),
                properties);

        FlinkKafkaProducer010<String> flinkProducer = new FlinkKafkaProducer010<String>(
                envMap.get(EnvKeys.KAFKA_TOPIC2.toString()).toString()
                , new SimpleStringSchema(),
                properties);


        //set up output directory
        final String processedImageDir = envMap.get(EnvKeys.OUT_DIR.toString()).toString();
        logger.warn("Output directory for saving processed images is set to " + processedImageDir + ". This is configured in processed.output.dir key of property file.");

        logger.info("framewidth and height to resize is set " + frameWidth + frameHeight  );
        DataStreamSink<String> stream = env.addSource(flinkConsumer)
                .keyBy("cameraId")
                .map(new TranscodeFunction(colorspace,frameWidth,frameHeight,processedImageDir ))
                .addSink(flinkProducer);


        // start the Flink stream execution
        if(execution_process.equals("FLINK")){
            env.execute();
        }
        //NES-ENABLEMENT
        else if(execution_process.equals("NES")) {
          logger.info("NES");
        }
        else{
            logger.error("EXECUTION_PROCESS is not set properly, EXECUTION_PROCESS="+execution_process);
        }    }
}
