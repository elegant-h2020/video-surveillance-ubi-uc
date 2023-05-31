# Architecture-Notes

* The architecture consists of two parts : the processor and the collector
(two modules therefore)

* In the collecor module one can find two classes : VideoStreamCollector and
VideoEventGenerator

* VideoStreamCollector is responsible for configuration 
of Kafka Producer and connect to Video-camera-url, also contains the main 
function

* VideoEventGenerator class is used to convert Video Frame into byte 
array and generate JSON event using Kafka Producer.

* Inside VideoStreamCollector class after Kafka Producer is created
a function that generates the events is called.

```java
		for(int i=0;i<urls.length;i++){
			Thread t = new Thread(new VideoEventGenerator(ids[i].trim(),urls[i].trim(),producer,topic));
			t.start();
		}
```

* The event generation function locates inside VideoGeneratorClass.
Firstly every frame is turned to a Mat object (OpenCV.Core) and 
then to a byte array 

```java
Mat mat = new Mat();
		Gson gson = new Gson();
		int frameCount = 0;
		while (camera.read(mat)) {
			//resize image before sending
			Imgproc.resize(mat, mat, new Size(640, 480), 0, 0, Imgproc.INTER_CUBIC);
			int cols = mat.cols();
	        int rows = mat.rows();
	        int type = mat.type();
			byte[] data = new byte[(int) (mat.total() * mat.channels())];
```

* Then the Kafka-event is send in Json format

```java
	        String json = gson.toJson(obj);
	        producer.send(new ProducerRecord<String, String>(topic,cameraId, json),new EventGeneratorCallback(cameraId));

```
* Constant values needed for configuration are read from a *resource* file
(stream-collector.properties, stream-processor.properties)

* In the processor module the main method lies in VideoStreamProcessor class

* VideoStreamProcessor class consumes Kafka messages and process them using
  Flink

* The main method follows the anatomy of a [flink program](program)

* FlinkKafaConsumer class is used to load initial data from a predfined kafka
  topic 

```java
        FlinkKafkaConsumer010<VideoEventStringData> flinkConsumer = new FlinkKafkaConsumer010 <VideoEventStringData>(prop.getProperty("kafka.topic"), new JsonDeserializer() , properties);

```
* In order to read the data, a deserialization scheme is needed

* Json Desirializer will tranform the json to VideoEventStringData
  class 

```java
    public VideoEventStringData deserialize(byte[] bytes) throws IOException {
        VideoEventStringData videoEvent = new VideoEventStringData();
        videoEvent = mapper.readValue(bytes, VideoEventStringData.class);
        return videoEvent;
```

* The VideoEventString Class has the following form

```java
public class VideoEventStringData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String cameraId;
    private String timestamp;
    private int rows;
    private int cols;
    private int type;
    private String data;

```

* Every chunk of data is processed using Flink`s map function 

```java
        DataStream<VideoEventStringData> stream = env.addSource(flinkConsumer)
                .keyBy("cameraId")
                .map(new StateFunction());
```

* The `NewStateFunction` is a class that contains the overidden map funtion

* Inside map funtion the `detectMotion` funtion gets activated.

```java
	public static VideoEventStringData detectMotion(String camId, Iterator<VideoEventStringData> frames,
													String outputDir, VideoEventStringData previousProcessedEventData) throws Exception {
		VideoEventStringData currentProcessedEventData = new VideoEventStringData();
		Mat frame = null;
		Mat copyFrame = null;
		Mat grayFrame = null;
		Mat firstFrame = null;
		Mat deltaFrame = new Mat();
		Mat thresholdFrame = new Mat();
		ArrayList<Rect> rectArray = new ArrayList<Rect>();

		// previous processed frame
		if (previousProcessedEventData != null) {
			logger.warn(
					"cameraId=" + camId + " previous processed timestamp=" + previousProcessedEventData.getTimestamp());
			Mat preFrame = getMat(previousProcessedEventData);
			Mat preGrayFrame = new Mat(preFrame.size(), CvType.CV_8UC1);
			Imgproc.cvtColor(preFrame, preGrayFrame, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(preGrayFrame, preGrayFrame, new Size(3, 3), 0);
			firstFrame = preGrayFrame;
		}

....
```
