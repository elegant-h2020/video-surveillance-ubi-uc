package eu.ubitech.video.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class VideoEventStringDeserializer  extends AbstractDeserializationSchema<VideoEventStringProcessed> {

    @Override
    public VideoEventStringProcessed deserialize( byte[] data) {
        ObjectMapper mapper = new ObjectMapper();
        VideoEventStringProcessed object = null;
        try {
            object = mapper.readValue(data, VideoEventStringProcessed.class);
        } catch (Exception exception) {
            System.out.println("Error in deserializing bytes "+ exception);
        }
        return object;
    }

}