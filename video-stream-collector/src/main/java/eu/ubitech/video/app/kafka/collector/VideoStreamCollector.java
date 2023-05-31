package eu.ubitech.video.app.kafka.collector;

import java.util.*;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.log4j.Logger;

import eu.ubitech.video.app.kafka.util.PropertyFileReader;

/**
 *  Class to configure Kafka Producer and connect to Video camera url.
 **/
public class VideoStreamCollector {

	private static final Logger logger = Logger.getLogger(VideoStreamCollector.class);

	public static void main(String[] args) throws Exception {

		// set producer properties
		Properties prop = PropertyFileReader.readPropertyFile();	
		Properties properties = new Properties();
		properties.put("bootstrap.servers", prop.getProperty("kafka.bootstrap.servers"));
		properties.put("acks", prop.getProperty("kafka.acks"));
		properties.put("retries",prop.getProperty("kafka.retries"));
		properties.put("batch.size", prop.getProperty("kafka.batch.size"));
		properties.put("linger.ms", prop.getProperty("kafka.linger.ms"));
		properties.put("max.request.size", prop.getProperty("kafka.max.request.size"));
		properties.put("compression.type", prop.getProperty("kafka.compression.type"));
		properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		// generate event
		Producer<String, String> producer = new KafkaProducer<String, String>(properties);
		//One-One correspondace between cameraIDs and cameraURLs
		List<String> cameraIDs = new ArrayList<>();
		Collections.addAll(cameraIDs,prop.getProperty("camera.id"),prop.getProperty("camera.id2"),prop.getProperty("camera.id3"));
		List<String> cameraURLs = new ArrayList<>();
		Collections.addAll(cameraURLs,prop.getProperty("camera.url"),prop.getProperty("camera.url2"),prop.getProperty("camera.url3"));
		generateIoTEvent(producer,prop.getProperty("kafka.topic"),cameraIDs,cameraURLs);
	}

	private static void generateIoTEvent(Producer<String, String> producer, String topic, List<String> ids, List<String> urls) throws Exception {

		if(urls.size() != ids.size()){
			throw new Exception("There should be same number of camera Id and url");
		}
		logger.info("Total urls to process "+urls.size());
		for(int i=0;i<urls.size();i++){
			Thread t = new Thread(new VideoEventGenerator(ids.get(i).trim(),urls.get(i).trim(),producer,topic));
			t.start();
		}
	}
}
