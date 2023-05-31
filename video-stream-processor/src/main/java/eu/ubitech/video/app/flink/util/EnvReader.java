package eu.ubitech.video.app.flink.util;


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

        String kafka_server = envMap.get(EnvKeys.BOOTSTRAP_SERVERS.toString());
        String kafka_topic= envMap.get(EnvKeys.KAFKA_TOPIC.toString());
        String kafka_topic2 =  envMap.get(EnvKeys.KAFKA_TOPIC2.toString());
        String proc_output_dir =  envMap.get(EnvKeys.PROCC_OUT_DIR.toString());
        String frame_output_dir =  envMap.get(EnvKeys.FRAM_OUT_DIR.toString());
        String mode = envMap.get(EnvKeys.MODE.toString());
        String xmlfile = envMap.get(EnvKeys.XML_FILE.toString());
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
            kafka_topic2="video-stream-out";
        }

        if(proc_output_dir != null){
            logger.info("Processed_output_dir is set to : " + proc_output_dir);
        } else{
            proc_output_dir="/tmp/processed-data/";
        }

        if(frame_output_dir != null){
            logger.info("Frame_output_dir is set to : " + frame_output_dir);
        } else{
            frame_output_dir="/tmp/frames/";
        }

        if(mode != null){
            logger.info("Mode is set to : " + mode);
        } else{
            mode="DEBUG";
        }

        if(xmlfile != null){
            logger.info("XML file is set to : " + xmlfile);
        } else{
            xmlfile="/tmp/haarcascade_frontalface_alt.xml";
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

        envMap.put(EnvKeys.BOOTSTRAP_SERVERS.toString(),kafka_server);
        envMap.put(EnvKeys.KAFKA_TOPIC.toString(),kafka_topic);
        envMap.put(EnvKeys.CAMERA_ID.toString(), cameraID);
        envMap.put(EnvKeys.KAFKA_TOPIC2.toString(),kafka_topic2);
        envMap.put(EnvKeys.PROCC_OUT_DIR.toString(),proc_output_dir);
        envMap.put(EnvKeys.FRAM_OUT_DIR.toString(),frame_output_dir);
        envMap.put(EnvKeys.XML_FILE.toString(),xmlfile);
        envMap.put(EnvKeys.MODE.toString(),mode);
        envMap.put("EXECUTION_PROCESS",execution_process);

        return envMap;
    }

}
