package eu.ubitech.video.app.util;


import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author plakic
 * @implNote This class is
 * responsible for reading and validating
 * env variables
 */
public class EnvReader {
    private static final Logger logger = Logger.getLogger(EnvReader.class);
    private static Map<String,String> envMap ;

    public EnvReader(HashMap<String,String> envMap){
        envMap = checkValues(envMap);
        logger.info("Checked enviroment variables Map");
        this.envMap = envMap ;
    }

    public void setEnvMap(Map<String, String> envMap) {
        this.envMap = envMap;
    }

    public  Map<String, String> getEnvMap() {
        return this.envMap;
    }

    /**
     * A method to check important variables
     * @param envMap
     * @return
     * @throws UnknownHostException
     */
    private HashMap<String, String> checkValues(HashMap<String, String> envMap) {

        String kafka_server = envMap.get(EnvKeys.KAFKA_BOOTSTRAP_SERVERS.toString());
        String kafka_topic= envMap.get(EnvKeys.KAFKA_TOPIC.toString());
        String kafka_topic2 =  envMap.get(EnvKeys.KAFKA_TOPIC2.toString());
        String base_path = envMap.get(EnvKeys.BASE_PATH.toString());
        String output_dir = envMap.get(EnvKeys.OUTPUT_DIR.toString());
        String face_folder = envMap.get(EnvKeys.FACE_FOLDER.toString());
        String execution_process = envMap.get("EXECUTION_PROCESS");

        if(kafka_server!=null){
            logger.info("Kafka Server is set to : " + kafka_server);
        } else{
            kafka_server="localhost:9092";
        }

        if(kafka_topic!=null){
            logger.info("Kafka topic is set to : " + kafka_topic +"\n");
        } else{
            kafka_topic="video-stream-out";
        }

        if(kafka_topic2!=null){
            logger.info("Kafka topic2 is set to : " + kafka_topic2 +"\n");
        } else {
            kafka_topic2 = "video-stream-similarity";
        }

        if(base_path!=null){
            logger.info("Base path is set to : " + base_path + "\n");
        } else {
            base_path = "/tmp/images/";
        }

        if(output_dir != null){
            logger.info("Save path is set to : " + output_dir);
        } else {
            output_dir = "/tmp/similarity/";
        }

        if(face_folder != null){
            logger.info("Face Folder  is set to : " + face_folder);
        } else {
            face_folder = "/tmp/face/";
        }

        if(execution_process != null){
            logger.info("EXECUTION_PROCESS is set to " + execution_process);
        } else{
            execution_process="FLINK";
        }


        envMap.put(EnvKeys.KAFKA_BOOTSTRAP_SERVERS.toString(),kafka_server);
        envMap.put(EnvKeys.KAFKA_TOPIC.toString(),kafka_topic);
        envMap.put(EnvKeys.KAFKA_TOPIC2.toString(),kafka_topic2);
        envMap.put(EnvKeys.BASE_PATH.toString(),base_path);
        envMap.put(EnvKeys.OUTPUT_DIR.toString(),output_dir);
        envMap.put(EnvKeys.FACE_FOLDER.toString(),face_folder);
        envMap.put("EXECUTION_PROCESS",execution_process);

        return envMap;
    }

}
