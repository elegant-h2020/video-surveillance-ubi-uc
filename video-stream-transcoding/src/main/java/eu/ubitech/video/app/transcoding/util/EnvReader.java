package eu.ubitech.video.app.transcoding.util;


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
        String output_dir = envMap.get(EnvKeys.OUT_DIR.toString());
        String colorspace = envMap.get(EnvKeys.COLORSPACE.toString());
        String frame_width = envMap.get(EnvKeys.FRAME_WIDTH.toString());
        String frame_height = envMap.get(EnvKeys.FRAME_HEIGHT.toString());
        String execution_process= envMap.get("EXECUTION_PROCESS");

        if(kafka_server!=null){
            logger.info("Kafka Server is set to : " + kafka_server);
        } else{
            kafka_server="localhost:9092";
        }

        if(kafka_topic!=null){
            logger.info("Kafka topic is set to : " + kafka_topic);
        } else{
            kafka_topic="video-stream-event";
        }

        if(kafka_topic2!=null){
            logger.info("Kafka topic2 is set to : " + kafka_topic2);
        } else{
            kafka_topic2="video-stream-transcoded";
        }

        if(output_dir != null){
            logger.info("Processed_output_dir is set to : " + output_dir);
        } else{
            output_dir="/tmp/transcoded/";
        }

        if(frame_width != null){
            logger.info("Frame_width is set to : " + frame_width);
        } else{
            frame_width="640";
        }

        if(frame_height != null){
            logger.info("Frame_height is set to : " + frame_height);
        } else{
            frame_height="480";
        }

        if(colorspace != null){
            logger.info("Colorspace is set to : " + colorspace);
        } else{
            colorspace="0";
        }


        if(execution_process != null){
            logger.info("EXECUTION_PROCESS is set to " + execution_process);
        } else{
            execution_process="FLINK";
        }


        //Get the hostname of computer
        String cameraID=null;
        try {
             cameraID = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Can't find host InetAddress.getLocalHost().getHostName()");
        }

        envMap.put(EnvKeys.KAFKA_BOOTSTRAP_SERVERS.toString(),kafka_server);
        envMap.put(EnvKeys.KAFKA_TOPIC.toString(),kafka_topic);
        envMap.put(EnvKeys.CAMERA_ID.toString(), cameraID);
        envMap.put(EnvKeys.KAFKA_TOPIC2.toString(),kafka_topic2);
        envMap.put(EnvKeys.OUT_DIR.toString(),output_dir);
        envMap.put(EnvKeys.COLORSPACE.toString(),colorspace);
        envMap.put(EnvKeys.FRAME_HEIGHT.toString(),frame_height);
        envMap.put(EnvKeys.FRAME_WIDTH.toString(),frame_width);
        envMap.put("EXECUTION_PROCESS",execution_process);

        return envMap;
    }

}
