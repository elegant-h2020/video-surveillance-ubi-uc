package eu.ubitech.video.app.transcoding.util;

/**
 * Kafka properites string const names
 **/
public enum KafkaPropertiesNames {
    BOOTSTRAP_SERVERS("bootstrap.servers"),
    ACKS("acks"),
    RETRIES("retries"),
    BATCH_SIZE("batch.size"),
    LINGER_MS("linger.ms"),
    MAX_REQUEST_SIZE("max.request.size"),
    COMPRESSION_TYPE("compression.type"),
    KEY_SERIALIZER("key.serializer"),
    VALUE_SERIALIZER("value.serializer"),
    GROUP_ID("group.id");

    private final String label;

    private KafkaPropertiesNames(String s){
        label = s ;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
