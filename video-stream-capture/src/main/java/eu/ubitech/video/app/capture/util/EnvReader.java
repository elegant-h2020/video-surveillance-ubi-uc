package eu.ubitech.video.app.capture.util;


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
        String ip = envMap.get(EnvKeys.FACE_DETECTION_IP.toString());
        String port = envMap.get(EnvKeys.FACE_DETECTION_PORT.toString());
        String capture_mode = envMap.get(EnvKeys.CAPTURE_MODE.toString());
        String file_loc = envMap.get(EnvKeys.VIDEO_FILE_LOCATION.toString());
        String kafka_server = envMap.get(EnvKeys.KAFKA_BOOTSTRAP_SERVERS.toString());
        String kafka_topic= envMap.get(EnvKeys.KAFKA_TOPIC.toString());

        String uri ;

        if(ip !=null && port != null ){
            logger.info("Env Variables set : FACE_DETECTION_IP=" + ip + " " +
                    "FACE_DETECTION_PORT=" + port);
        }else{
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.error("Unkown Host");
            }
            port = "8080";
        }
        uri = ip +":"+ port;

        if(capture_mode!=null){
            logger.info("Video Capture mode is set to : " + capture_mode);
        } else{
            capture_mode="REAL";
        }

        if(file_loc!=null){
            logger.info("File location is set to : " + file_loc);
        } else{
            file_loc="/tmp/business.mp4";
        }

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

        //Get the hostname of computer
        String cameraID=null;
        try {
             cameraID = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Can't find host InetAddress.getLocalHost().getHostName()");
        }


        logger.info("The uri to send the data to is " +uri);


        //Todo : add Cameraid
        envMap.put(EnvKeys.FACE_DETECTION_IP.toString(),ip);
        envMap.put(EnvKeys.FACE_DETECTION_PORT.toString(),port);
        envMap.put(EnvKeys.CAPTURE_MODE.toString(),capture_mode);
        envMap.put(EnvKeys.VIDEO_FILE_LOCATION.toString(),file_loc);
        envMap.put(EnvKeys.FACE_DETECTION_URI.toString(),uri);
        envMap.put(EnvKeys.KAFKA_BOOTSTRAP_SERVERS.toString(),kafka_server);
        envMap.put(EnvKeys.KAFKA_TOPIC.toString(),kafka_topic);
        envMap.put(EnvKeys.CAMERA_ID.toString(), cameraID);



        return envMap;
    }

}
