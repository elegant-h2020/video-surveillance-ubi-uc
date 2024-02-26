# ELEGANT's Surveillance-Use-Case 

A collection of modules reqarding the ELEGANT project

## Character-Recognition module 

This module utilizes a pre-trained handwriting model to recognize and extract text from images. The 
input is given as a CSV file which contains rows of Base64 encoded images along with the ground truth
text of the image. The processing is handled by Nebulastream which makes uses of a Java UDF Mapper 
alongside with OpenCV library to process the input images, and uses the handwriting model for inference.
The output is again a CSV file with the ocr Text and the real text for each input image.

```java
    // Classes for Video Frames Pre Processing UDF
    static class InputImage {
        String encodedInputImage;
        String realText;
    }

    static class OutputText {
        String ocrText;
        String realText;
    }

    static class InputImageMapper implements MapFunction<InputImage, OutputText> {


        public OutputText map(final InputImage inputImage) {
            Model ocrModel = OcrProcessor.loadOCRmodel("/ubidemo/handwriting.model");
            OutputText outputText = new OutputText();
            outputText.ocrText = OcrProcessor.processImage(inputImage.encodedInputImage, (ComputationGraph) ocrModel);
            outputText.realText = inputImage.realText;
            return outputText;
        }
    }
```

To run: 

```
cd character-recognition
mvn clean package
java -jar target/character-recognition-[version].jar
```

## Vulnerabilities-assessor

This module accepts a CSV file with vulnerabilities report generated for a microservice, and uses Nebulastream
library to performs filter and join operations based on type of vulnerabilities as well as a custom Java UDF 
to assign the severity level of the vulnerabilty (LOW, MEDIUM, HIGH, CRITICAL, UNKNOWN).

```java

class CvssInput {
    String name;
    float cvssscore;
}

class CvssOutput {
    String name;
    String severity;
}

// This UDF determines the severity level based on the cvssscore
class CvssScoreToSeverityV2 implements MapFunction<CvssInput, CvssOutput> {
    public CvssOutput map(final CvssInput input) {
        CvssOutput output = new CvssOutput();
        output.name = input.name;
        if (input.cvssscore >= 0 && input.cvssscore <= 3.9) {
            output.severity = "LOW";
        } else if (input.cvssscore >= 4 && input.cvssscore <= 6.9) {
            output.severity = "MEDIUM";
        } else if (input.cvssscore >= 7 && input.cvssscore <= 10) {
            output.severity = "HIGH";
        } else {
            output.severity = "UNKNOWN"; // cvssscore is out of expected range
        }
        return output;
    }
}
```

To run:

```
cd vulnerabilities-assessor
mvn clean package
java -jar target/vulnerabilities-assessor-[version].jar
```

## Video-meta-analytics-module

This module utilizes the NebulaStream Java Client to connect to a running nebulastream configuration
and submit queries. The module contains a docker-compose from where you can instantiate a coordinator
and a worker, which are the essential services of nebulastream. Under resources there are some sample  
input sources to be used.  Sample configurations are given in coordinator.yml and worker-1.yml. There 
is also a set of configurations under /distributed_configurations which contains the configuration 
files (docker-compose.yml, coordinator.yml, worker.yml) for distributed topologies. 

A sample Query through Java Client (more can be foynd in src/main/java/analytics/VideoAnaltyicsMeta.main):

 ```java
private static void dataPreProcessing(NebulaStreamRuntime ner, String stream_name) throws  Exception {

        // Select a source from the registered ones
        Query worker = ner.readFromSource(stream_name);

        // Filter Operator
        worker.filter(attribute("num_faces").lessThan(3));

        // Map Operator
        worker.map("frame_width", Expressions.literal(780));
        worker.map("frame_height", Expressions.literal(310));
        worker.map("colorspace", Expressions.literal(83));

        // Sink Operator (FileSink)
        worker.sink(new FileSink("/output_dataPreProcessing_query.csv", "CSV_FORMAT", true));

        // Get Query ID
        int queryId = ner.executeQuery(worker, "BottomUp");
        System.out.println("Query placed for execution with id: " + queryId);
        System.out.println(" *** \n");
    }
```

The docker-compose offers also extra services such as the Elegant Devops Dashboard which is a web UI to visualize
the configuration, queries, sources etc.

## Experiments

Under the /experiments directory there is a dedicated Python script for processing the query results and creating plots 
for the execution times of queries on two different topologies, with 4 different input sizes. The /query_measurements 
directory has to contain the execution results obtained from NES, following the name convention: "qX-sY-tZ.json" (where
X=1-8 for queries, Y=1-4 for input size, and Z=1-2 for topology). For example, q2-s3-t2.json are the execution results
of query #2 (dataPreProcessingDistributedwithMap) with input size 3 (in our case 10MB) running in topology 2 (Edge).
Under /plots_execution_times there can be found the generated performance graphs, and under /query_plan_figures the query 
plans are stored as image files.

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

## Instructions to run  Capture, Processor, Similarity modules 
In order to run the  code-base OpenCV needs to be installed thats why,
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