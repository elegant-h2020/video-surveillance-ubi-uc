package eu.ubitech.video.app.flink.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ubitech.video.app.flink.util.VideoEventStringProcessed;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class CustomSerializerSchema implements SerializationSchema<String> {

    private static final long serialVersionUID = 1L;

    @Override
    public byte[] serialize(String element) {
        byte[] b = null;
        try {
            b= new ObjectMapper().writeValueAsBytes(element);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return b;
    }
}