package eu.ubitech.video.app.similarity.processor;

import eu.ubitech.video.app.util.PropertiyFileReader;
import eu.ubitech.video.app.util.VideoEventStringDeserializer;
import eu.ubitech.video.app.util.VideoEventStringProcessed;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;


public class KafkaDataConsumer {

    //Create and subscribe Consumer
    public static Consumer getConsumer() throws Exception {
        Properties properties = new Properties();
        Properties prop = PropertiyFileReader.readPropertyFile();
        properties.put("bootstrap.servers", prop.getProperty("kafka.bootstrap.servers"));
        properties.put("acks", prop.getProperty("kafka.acks"));
        properties.put("retries",prop.getProperty("kafka.retries"));
        properties.put("batch.size", prop.getProperty("kafka.batch.size"));
        properties.put("linger.ms", prop.getProperty("kafka.linger.ms"));
        properties.put("max.request.size", prop.getProperty("kafka.max.request.size"));
        properties.put("compression.type", prop.getProperty("kafka.compression.type"));
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, VideoEventStringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,prop.getProperty("kafka.group_id"));


        Consumer<String,VideoEventStringProcessed> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(prop.getProperty("kafka.topic")));
        return consumer;
    }


}
