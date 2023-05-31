# ELEGANT's Surveillance-Use-Case 

A collection of modules reqarding the ELEGANT project

## Video-capture-module
This module is responsible for capturing 
video-frames(/dev/video0) using the OpenCV library 
and sening them with additional information 
(metatdata:*CameraID,timestamp,rows,cols* )

the block of code containing that info is under 
 VideoEventGeneretor Class
 ```java
 byte[] data = new byte[(int) (mat.total() * mat.channels())];
            mat.get(0, 0, data);
String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            JsonObject obj = new JsonObject();
            //Json Object that will be send as message
            obj.addProperty("cameraId",cameraId);
            obj.addProperty("timestamp", timestamp);
            obj.addProperty("rows", rows);
            obj.addProperty("cols", cols);
            obj.addProperty("type", type);
            obj.addProperty("data", Base64.getEncoder().encodeToString(data));
            String json = gson.toJson(obj);
            producer.send(new ProducerRecord<String, String>(topic,cameraId, json),new EventGeneratorCallback(cameraId));
            logger.info("Generated events for cameraId=" + cameraId + " frameid=" + (++frameCount) + " timestamp="+timestamp);
```
The info is send via json-message to Kafka. Also in order
to retrive the frames and further process them OpenCV is 
used.
## Video-Processor
In this module, Flink and OpenCV is utilized for frame-processing.
Through Flink, json messages are retrived from Kafka (*which where send to by video
capturing module*). 
```java
        DataStreamSink<String> stream = env.addSource(flinkConsumer)
                .keyBy("cameraId")
                .map(new TransformFunction())
                .filter(new FaceDataFilter())
                .addSink(flinkProducer);
```

Then Flink's map operation starts (RichMap-Functionality).
Every piece of data is processed via *TranformFunction*.
In that function each json message is desirialized and the frame (binary-fromat
is retrived). `detectFacce` then hanles the frame-processed by examing the
location of faces in it. 

```java

        VideoEventStringProcessed videoEventStringProcessed = new VideoEventStringProcessed(
                videoEventStringData.getCameraId(),
                videoEventStringData.getTimestamp(),
                videoEventStringData.getRows(),
                videoEventStringData.getCols(),
                videoEventStringData.getType(),
                videoEventStringData.getData(),
                lista
        );

        return videoEventStringProcessed;
    }
```
The function will return original data with an additional list. That list contains
info of the points(bottom-left,top-right) that are needed to represent a facial-area.
. The **return** of *map* is a string containing the jsonized class mentioned
above.Then the filter operation will eliminate the data(frames) that does not
contain face by checking the *list* property. Data are sinked for further processing.

## Video-Stream-Similarity 
This module is responsible for exposing the similarity of faces in frames.
```java
        DataStreamSink<String> stream = env.addSource(flinkConsumer)
                .map(new SimilarityProcess())
                .addSink(flinkProducer);
```

The `SimilarityProcess` is the following:
```java
public class SimilarityProcess extends RichMapFunction<VideoEventStringProcessed, String> {
    @Override
    public String map(VideoEventStringProcessed videoEventStringProcessed) throws Exception {
        faceSimilarity.loadModel();
        List<Mat> corpFaces = FaceProcessor.corpFaces(videoEventStringProcessed);
        for (int i =0 ; i < corpFaces.size();i++){
            //Get Simialarity
            SimilarityProcessor.saveImage(corpFaces.get(i),videoEventStringProcessed,"/tmp/similarity/");
            faceSimilarity.whoIs(corpFaces.get(i));
        }
        //TODO:return something decent
        return "i did the similarity";
    }
```
By using OpenCV firstly we corp  the frames, amd tjem feed them to the ML model.
The `whoIs` function will output the similarity measurment between the input image/face
and pre-detrmined ones.

#Video-Stream-Transcoding/Preprocessing 
The flow of this module using Flink and OpenCV is the following: 
```java
        DataStreamSink<String> stream = env.addSource(flinkConsumer)
                .keyBy("cameraId")
                .map(new TranscodeFunction(colorspace,frameWidth,frameHeight,processedImageDir))
                .addSink(flinkProducer);
```
Inside map function each frame will go under changes:
```java
  @Override
public String map(VideoEventStringData videoEventStringData) throws Exception {
        //Get frame from image
        Mat data = getMat(videoEventStringData);
        Mat dataProcessed = changeFrameResolution(data,frameWidth,frameHeight);
        dataProcessed = changeFrameColorSpace(dataProcessed,colorspace);
        byte[] encodedImage = encodeData(dataProcessed);
        saveImage(dataProcessed,videoEventStringData,outputDir);

        Gson gson = new Gson();
        JsonObject obj = new JsonObject();
        //Json Object that will be send as message
        obj.addProperty("cameraId",videoEventStringData.getCameraId());
        obj.addProperty("timestamp", videoEventStringData.getTimestamp());
        obj.addProperty("rows", videoEventStringData.getRows());
        obj.addProperty("cols", videoEventStringData.getCols());
        obj.addProperty("type", videoEventStringData.getType());
        obj.addProperty("data", Base64.getEncoder().encodeToString(encodedImage));

        String json = gson.toJson(obj);


        return json;
```
Frameheight,Framewidth and colorspace will be changed.
The result will be a json string tha will be sunk to a Kafka 
Producer through flink's Map-Operator.

## Instructions to run 
In order to run the  code-base OpenCv needs to be installed thats why,
thats why `install.sh` has been provided under the `video-stream
-processor` module.
Then :
1. Run `start.sh`. This script creates directories and
copies files needed for the modules to run sucessfully.
Also run a Kafka-docker image
2. `cd` into the video-stream-processor module and
run 
```
mvn clean package
java -jar target/video-stream-processor*
```
This will start the processor module.
3. `cd` to the video-processor module and follow the 
same flow to start capturing frames
```
mvn clean package
java -jar target/video-stream-capture*
```
4. Same goes for video-stream similarity 

## Instructions to run Dockerized
1. `cd` into video-processor
`docker run --network=host --env-file src/main/resources/processor.env processor
`
2. `cd` into video-similarity 
`docker run --network=host --env-file src/main/resources/similarity.env  -it   similarity-image
`
3. `cd` into video-capture 
 `docker run --network=host --env-file=src/main/resources/capture.env --device=/dev/video0:/dev/video0 capture:latest
   `