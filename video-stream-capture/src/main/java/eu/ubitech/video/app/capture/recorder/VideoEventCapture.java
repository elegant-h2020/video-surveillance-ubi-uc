package eu.ubitech.video.app.capture.recorder;

import eu.ubitech.video.app.capture.util.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.log4j.Logger;

import java.util.*;

public class VideoEventCapture {

    private static final Logger logger = Logger.getLogger(VideoEventCapture.class);

    public static void main(String[] args) throws Exception {

        //Get env variables
        HashMap<String, String> envHashMap = new HashMap<String, String>(System.getenv());
        EnvReader envReader = new EnvReader(envHashMap);

        // Get all the env variables
        Map<String,String> envMap = envReader.getEnvMap();

        // Set producer properties
        Properties properties = new Properties();


        properties.put(KafkaPropertiesNames.BOOTSTRAP_SERVERS.toString(),envMap.get("BOOTSTRAP_SERVERS").toString());
        properties.put(KafkaPropertiesNames.ACKS.toString(),envMap.get("ACKS").toString());
        properties.put(KafkaPropertiesNames.RETRIES.toString(),envMap.get("RETRIES").toString());
        properties.put(KafkaPropertiesNames.BATCH_SIZE.toString(),envMap.get("BATCH_SIZE").toString());
        properties.put(KafkaPropertiesNames.LINGER_MS.toString(),envMap.get("LINGER_MS").toString());
        properties.put(KafkaPropertiesNames.MAX_REQUEST_SIZE.toString(),envMap.get("MAX_REQUEST_SIZE").toString());
        properties.put(KafkaPropertiesNames.COMPRESSION_TYPE.toString(),envMap.get("COMPRESSION_TYPE").toString());
        properties.put(KafkaPropertiesNames.KEY_SERIALIZER.toString(),envMap.get("KEY_SERIALIZER").toString());
        properties.put(KafkaPropertiesNames.VALUE_SERIALIZER.toString(),envMap.get("VALUE_SERIALIZER").toString());


        printKafkaProperties(properties);


        //Check if property entity is null
        Map<String,String> map =new HashMap<>();
        for (final String name: properties.stringPropertyNames())
            map.put(name, properties.getProperty(name));
         checkForNull(map);


        Producer<String, String> producer = new KafkaProducer<String, String>(properties);



        //Initialize EventHandler
        IotEventHandler event =  new IotEventHandler.IotEventBuilder(producer,
                //Map[key]-->value
                envMap.get(EnvKeys.KAFKA_TOPIC.toString()))
                .captureMode(envMap.get(EnvKeys.CAPTURE_MODE.toString()))
                //method(string)
                .cameraId(envMap.get(EnvKeys.CAMERA_ID.toString()))
                .fileLocation(envMap.get(EnvKeys.VIDEO_FILE_LOCATION.toString()))
                .uriSet(envMap.get(EnvKeys.FACE_DETECTION_URI.toString()))
                .build();

        //Pass metadata for IoTEvent processing
        generateIoTEvent(event);
    }

    /**
     * A function that prints Kafka Properties
     * set from env variables
     * @param properties
     */
    private static void printKafkaProperties(Properties properties) {
        for ( Object name: properties.keySet()) {
            String key = name.toString();
            String value = properties.get(name).toString();
            logger.info("Key is " + key + " and values is "+value);
        }
    }

    /**
     * The main function. Generates a thread, that starts
     * capturing process
     * @param event contains metadata
     * @throws Exception
     */
    private static void generateIoTEvent(IotEventHandler event) throws Exception {
        logger.info("Statring process");
        VideoEventGenerator vidEvent = transformEvent(event);
        Thread t = new Thread(vidEvent);
        //Thread t = new Thread(new VideoEventGenerator(ids.get(i).trim(),urls.get(i).trim(),producer,topic));
        t.start();
    }

    /**
     * Copies metadata of IotEventHandler class
     * to VideoEventGenerator
     * @param event
     * @return
     */
    private  static VideoEventGenerator transformEvent(IotEventHandler event){
        String id = event.getCameraId();
        String uriIp = event.getUri();
        String capture_mode = event.getCapture_mode();
        String file_loc = event.getFile_location();
        Producer<String, String> producer = event.getKafka_producer();
        String kafka_topic = event.getKafka_topic();

        VideoEventGenerator vidEvent = new VideoEventGenerator.
                VideoEventGenBuilder(producer,kafka_topic)
                .captureMode(capture_mode)
                .cameraId(id)
                .uri(uriIp)
                .fileLoc(file_loc)
                .build();
        return vidEvent;
    }

    /**
     * Check is all values are there
     * @param map
     */
    public static void checkForNull(Map<String,String> map){
        Set<String> keyies = map.keySet();
        for (String e : keyies){
            String value = map.get(e);
            if(value.isEmpty()){
                logger.error("Key "+e+"in env Variables hashMap does not contain value");
                System.exit(-1);
            }
        }
        return ;
    }
}
