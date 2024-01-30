package analytics;

import stream.nebula.expression.Expressions;
import stream.nebula.operators.Aggregation;
import stream.nebula.operators.sinks.FileSink;
import stream.nebula.operators.sinks.KafkaSink;
import stream.nebula.operators.window.EventTime;
import stream.nebula.operators.window.TimeMeasure;
import stream.nebula.operators.window.TumblingWindow;
import stream.nebula.runtime.NebulaStreamRuntime;
import stream.nebula.runtime.Query;
import stream.nebula.udf.MapFunction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static stream.nebula.expression.Expressions.attribute;

/**
 * @author Vasileios Matsoukas, Ioannis Plakas
 * @email vmatsoukas@ubitech.eu, iplakas@ubitech.eu,
 * @date ${DATE}
 */
public class VideoAnalyticsMeta {

    // Deprecated Usage
    private static void add_stream(NebulaStreamRuntime ner, String stream_name) throws Exception {

        SchemeBuilder builder = new SchemeBuilder();
/*
        colorspace,frame_width,frame_height,node_name,tmst,num_faces,valueImageRaw
*/
        builder.addField("colorspace", BasicDataType.UINT64)
                .addField("frame_width", BasicDataType.UINT32)
                .addField("frame_height", BasicDataType.UINT32)
                .addStringField("node_name", 50)
                .addStringField("date",50)
                .addField("tmst_sec",BasicDataType.UINT64)
                .addField("tmst",BasicDataType.UINT64)
                .addField("num_faces", BasicDataType.UINT64)
                .addStringField("imageID", 40);
        boolean added = ner.addLogicalSource(stream_name, builder.build());
        System.out.println("Added : " + added);

          List<String> StreamCatalog = ner.getLogicalSources();
          System.out.println("STREAMS : " + StreamCatalog);
    }

    // Deprecated Usage
    private static void add_kafka_stream(NebulaStreamRuntime ner, String stream_name) throws Exception {

        SchemeBuilder builder = new SchemeBuilder();
        builder.addStringField("date",50);

        boolean added = ner.addLogicalSource(stream_name, builder.build());
        System.out.println("Added : " + added);

        List<String> StreamCatalog = ner.getLogicalSources();
        System.out.println("STREAMS : " + StreamCatalog);
    }

    // Data Pre Processing Query
    private static void dataPreProcessing(NebulaStreamRuntime ner, String stream_name) throws  Exception {

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

    // Data Preprocessing Query: Distributed Version with 3 workers
    private static void dataPreProcessingDistributed(NebulaStreamRuntime ner,String stream_name_1, String stream_name_2, String stream_name_3) throws  Exception {

        // Define multiple workers
        Query worker1 = ner.readFromSource(stream_name_1);
        Query worker2 = ner.readFromSource(stream_name_2);
        Query worker3 = ner.readFromSource(stream_name_3);


        // Operations for worker1: Filter, Map, Map, Map, Sink (FileSink)
        worker1.filter(attribute("num_faces").lessThan(3));
        worker1.map("frame_width", Expressions.literal(780));
        worker1.map("frame_height", Expressions.literal(310));
        worker1.map("colorspace", Expressions.literal(83));
        worker1.sink(new FileSink("/output_dataPreProcessingDistributed_query_worker_1.csv", "CSV_FORMAT", true));
        int queryIdWorker1 = ner.executeQuery(worker1, "BottomUp");

        // Operations for worker2: Filter, Map, Map, Map, Sink (FileSink)
        worker2.filter(attribute("num_faces").lessThan(3));
        worker2.map("frame_width", Expressions.literal(780));
        worker2.map("frame_height", Expressions.literal(310));
        worker2.map("colorspace", Expressions.literal(83));
        worker2.sink(new FileSink("/output_dataPreProcessingDistributed_query_worker_2.csv", "CSV_FORMAT", true));
        int queryIdWorker2 = ner.executeQuery(worker2, "BottomUp");

        // Operations for worker3: Filter, Map, Map, Map, Sink (FileSink)
        worker3.filter(attribute("num_faces").lessThan(3));
        worker3.map("frame_width", Expressions.literal(780));
        worker3.map("frame_height", Expressions.literal(310));
        worker3.map("colorspace", Expressions.literal(83));
        worker3.sink(new FileSink("/output_dataPreProcessingDistributed_query_ worker_3.csv", "CSV_FORMAT", true));
        int queryIdWorker3 = ner.executeQuery(worker3, "BottomUp");

        System.out.println("QueryW1 placed for execution with id: " + queryIdWorker1);
        System.out.println("QueryW2 placed for execution with id: " + queryIdWorker2);
        System.out.println("QueryW3 placed for execution with id: " + queryIdWorker3);

        System.out.println(" *** \n");
    }

    // Crowd Estimate Query
    private static void crowdEstimate(NebulaStreamRuntime ner,String stream_name) throws  Exception {

        Query worker = ner.readFromSource(stream_name);

        // Window Operator chained with Group By and Sum
        worker.window(TumblingWindow.of(EventTime.eventTime("tmst"), TimeMeasure.minutes(1))).
                byKey("node_name").apply(Aggregation.sum("num_faces"));

        // Sink Operator (FileSink)
        worker.sink(new FileSink("/output_crowdEstimate_query.csv", "CSV_FORMAT", true));

        // Get Query ID
        int queryId = ner.executeQuery(worker, "BottomUp");
        System.out.println("Query placed for execution with id: " + queryId);
        System.out.println(" *** \n");
    }

    // Filter Timestamps Query
    private static void filterTimestamps(NebulaStreamRuntime ner,String stream_name) throws  Exception {

        Query worker = ner.readFromSource(stream_name);

        // Filter Operator
        //only values greater than /4/12/2022 1:53:55 in epoch time
        worker.filter(attribute("tmst_sec").greaterThan(1649760835));
        worker.filter(attribute("num_faces").equalTo(1));

        // Sink Operator (FileSink)
        worker.sink(new FileSink("/output_timestamp_query.csv", "CSV_FORMAT", true));

        // Get Query ID
        int queryId = ner.executeQuery(worker, "BottomUp");
        System.out.println("Query placed for execution with id: " + queryId);
        System.out.println(" *** \n");
    }

    // Read From Kafka Query
    private static void readFromKafka(NebulaStreamRuntime ner,String stream_name) throws  Exception {

        Query worker = ner.readFromSource(stream_name);

        // Sink Operator (FileSink)
        worker.sink(new FileSink("/output_kafka_query.csv", "CSV_FORMAT", true));

        // Get Query ID
        int queryId = ner.executeQuery(worker, "BottomUp");
        System.out.println("Query placed for execution with id: " + queryId);
        System.out.println(" *** \n");
    }

    // Read From Kafka and Store to Kafka Query
    private static void readFromKafkaStoreToKafka(NebulaStreamRuntime ner, String stream_name) throws  Exception {

        Query worker = ner.readFromSource(stream_name);

        // Sink Operator (KafkaSink)
        worker.sink(new KafkaSink("example-output", "212.101.173.100:29092", 10000));

        // Get Query ID
        int queryId = ner.executeQuery(worker, "BottomUp");
        System.out.println("Query placed for execution with id: " + queryId);
        System.out.println(" *** \n");
    }

    // Video Frames Pre Processing using Java UDF
    private static void videoFramesPreProcessingWithMap(NebulaStreamRuntime ner,String stream_name) throws  Exception {

        Query worker = ner.readFromSource(stream_name);

        // Project Operator chained with Map (Java UDF) operator
        worker.project(attribute("frame"), attribute("number"))
                .map(new FrameMapper());

        // Sink Operator (FileSink)
        worker.sink(new FileSink("/output_videoFramesPreProcessingWithMap_query.csv", "CSV_FORMAT", true));

        // Get Query ID
        int queryId = ner.executeQuery(worker, "BottomUp");
        System.out.println("Query placed for execution with id: " + queryId);
        System.out.println(" *** \n");
    }

    // Classes for Video Frames Pre Processing UDF
    static class InputFrame {
        String frame;
        int number;
    }

    static class OutputFrame {
        String frame;
        int number;
    }

    static class FrameMapper implements MapFunction<InputFrame, OutputFrame> {
        public OutputFrame map(final InputFrame inputFrame) {
            OutputFrame outputFrame =  new OutputFrame();
            outputFrame.frame = ImageProcessingTools.encodedStringImageToGrayScaleEncodedStringImage(inputFrame.frame);
            outputFrame.number = inputFrame.number;
            return outputFrame;
        }
    }


    // Age Data Pre Processing using Java UDF
    private static void ageProcessingWithMap(NebulaStreamRuntime ner, String stream_name) throws  Exception {

        Query worker = ner.readFromSource(stream_name);

        // Project Operator chained with Map (Java UDF) operator
        worker.project( attribute("timestamp"), attribute("age"))
                .map(new AgeMapper());

        // Sink Operator (FileSink)
        worker.sink(new FileSink("/output_ageProcessingWithMap_query.csv", "CSV_FORMAT", true));

        // Get Query ID
        int queryId = ner.executeQuery(worker, "BottomUp");
        System.out.println("Query placed for execution with id: " + queryId);
        System.out.println(" *** \n");
    }

    // Classes for Age Data Pre Processing UDF
    static class InputAge {
        String timestamp;
        int age;
    }

    static class OutputAge {
        String timestamp;
        int age;
    }

    static class AgeMapper implements MapFunction<InputAge, OutputAge> {
        public OutputAge map(final InputAge inputAge) {
            OutputAge outputAge =  new OutputAge();
            outputAge.timestamp = inputAge.timestamp;
            outputAge.age = inputAge.age;
            return outputAge;
        }
    }

    public static void main(String[] args) throws Exception {
        String stream_name_1 = "Transc_stream";
        String stream_name_2 = "Transc_stream_1mb";
        String stream_name_3 = "Transc_stream_1mb";
        String stream_name_4 = "Transc_stream_100mb";
        String stream_name_5 = "Transc_stream_2";
        String stream_ages_name = "ages";
        String stream_video_name_1 = "video";
        String stream_video_name_2 = "video2";
        String stream_video_name_3 = "video3";
        String stream_video_name_4 = "video4";
        String kafka_source_name = "kafka_source";


        // Configure network connection to NES REST server
        System.out.println(" *** connection:\n");
        NebulaStreamRuntime ner = NebulaStreamRuntime.getRuntime();
        ner.getConfig().setHost("192.168.7.40").setPort("8081");
        System.out.println(" *** connected:\n");

        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String input = "";

        while (!input.equals("q")){
            System.out.println("Type '0' to create a new logical stream," +
                    "\n '1' to execute dataPreProcessing query," +
                    "\n '2' to execute dataPreProcessingDistributed query," +
                    "\n '3' to execute crowdEstimate query," +
                    "\n '4' to execute fiterTimestamp query," +
                    "\n '5' to execute Kafka readi query," +
                    "\n '6' to execute Kafka read and store query," +
                    "\n '7' to execute videoFramesPreProcessingWithMap query," +
                    "\n '8' to execute ageProcessingWithMap query," +
                    "\n 'q' to exit, then press ENTER : ");

            // Reading data using readLine
            input = reader.readLine();

            if(input.equals("0")) {
                  add_kafka_stream(ner, kafka_source_name);
                  add_stream(ner, stream_name_1);
            }
            else if (input.equals("1")) {
                dataPreProcessing(ner, stream_name_1);
                TimeUnit.SECONDS.sleep(20);
                dataPreProcessing(ner, stream_name_2);
                TimeUnit.SECONDS.sleep(20);
                dataPreProcessing(ner, stream_name_3);
                TimeUnit.SECONDS.sleep(20);
                dataPreProcessing(ner, stream_name_4);
            }
            else if (input.equals("2")) {
                dataPreProcessingDistributed(ner, stream_name_1, stream_name_1, stream_name_1);
                TimeUnit.SECONDS.sleep(20);
                dataPreProcessingDistributed(ner, stream_name_2, stream_name_2, stream_name_2);
                TimeUnit.SECONDS.sleep(20);
                dataPreProcessingDistributed(ner, stream_name_3, stream_name_3, stream_name_3);
                TimeUnit.SECONDS.sleep(20);
                dataPreProcessingDistributed(ner, stream_name_4, stream_name_4, stream_name_4);
            }
            else if (input.equals("3")) {
                crowdEstimate(ner, stream_name_1);
//                TimeUnit.SECONDS.sleep(20);
//                crowdEstimate(ner, stream_name_2);
//                TimeUnit.SECONDS.sleep(20);
//                crowdEstimate(ner, stream_name_3);
//                TimeUnit.SECONDS.sleep(20);
//                crowdEstimate(ner, stream_name_4);
            }
            else if (input.equals("4")) {
                filterTimestamps(ner, stream_name_1);
                TimeUnit.SECONDS.sleep(20);
                filterTimestamps(ner, stream_name_2);
                TimeUnit.SECONDS.sleep(20);
                filterTimestamps(ner, stream_name_3);
                TimeUnit.SECONDS.sleep(20);
                filterTimestamps(ner, stream_name_4);
                TimeUnit.SECONDS.sleep(20);
            }
            else if (input.equals("5"))
                readFromKafka(ner, kafka_source_name);
            else if (input.equals("6"))
                readFromKafkaStoreToKafka(ner, kafka_source_name);
            else if (input.equals("7")) {
                videoFramesPreProcessingWithMap(ner, stream_video_name_1);
                 TimeUnit.SECONDS.sleep(20);
                videoFramesPreProcessingWithMap(ner, stream_video_name_2);
                TimeUnit.SECONDS.sleep(20);
                videoFramesPreProcessingWithMap(ner, stream_video_name_3);
                TimeUnit.SECONDS.sleep(20);
                videoFramesPreProcessingWithMap(ner, stream_video_name_4);
                TimeUnit.SECONDS.sleep(20);
            }
            else if (input.equals("8"))
                ageProcessingWithMap(ner, stream_ages_name);

        }
        // Printing the read line
        System.out.println("Exiting...");
   }
}