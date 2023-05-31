package eu.ubitech.video.app.capture.util;

import org.apache.kafka.clients.producer.Producer;

public class IotEventHandler {
    //Required fields
    private final Producer<String, String> kafka_producer;
    private final String kafka_topic;
    private final String uri;
    private final String cameraId;
    private final String capture_mode;
    private final String file_location;


    public String getUri() {
        return uri;
    }

    public String getCameraId() {
        return cameraId;
    }

    public String getCapture_mode() {
        return capture_mode;
    }

    public String getFile_location() {
        return file_location;
    }

    public Producer<String, String> getKafka_producer() {
        return kafka_producer;
    }

    public String getKafka_topic() {
        return kafka_topic;
    }

    private IotEventHandler(String uri, String cameraId, String capture_mode, String file_location, Producer<String, String> kafka_producer, String kafka_topic) {
        this.uri = uri;
        this.cameraId = cameraId;
        this.capture_mode = capture_mode;
        this.file_location = file_location;
        this.kafka_producer = kafka_producer;
        this.kafka_topic = kafka_topic;
    }

    public static class IotEventBuilder{
        private  String nestedUri ;
        private  String nestedCameraId;
        private  String nestedCaptureMode;
        private  String nestedFileLocation;
        private final Producer<String, String> nestedKafkaProducer;
        private final String nestedKafkaTopic;

        public IotEventBuilder (Producer<String, String> kafkaProducer, String kafkaTopic){
            this.nestedKafkaProducer = kafkaProducer ;
            this.nestedKafkaTopic = kafkaTopic ;
        }

        public IotEventBuilder captureMode(String capture_mode){
            this.nestedCaptureMode = capture_mode;
            return this;
        }

        public IotEventBuilder cameraId(String cameraId){
            this.nestedCameraId = cameraId;
            return this;
        }

        public IotEventBuilder fileLocation(String file_location){
            this.nestedFileLocation = file_location;
            return this;
        }

        public IotEventBuilder uriSet(String uriIp){
            this.nestedUri = uriIp ;
            return this;
        }

        public IotEventHandler build(){
            return new IotEventHandler(this.nestedUri,this.nestedCameraId,
                    this.nestedCaptureMode,
                    this.nestedFileLocation,
                    this.nestedKafkaProducer,
                    this.nestedKafkaTopic
            );
        }

    }


}
