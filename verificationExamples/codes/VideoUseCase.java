package codes;

import stream.nebula.operators.sinks.FileSink;
import stream.nebula.runtime.NebulaStreamRuntime;
import stream.nebula.runtime.Query;
import stream.nebula.udf.MapFunction;

import static stream.nebula.expression.Expressions.attribute;

public class VideoUseCase {


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
            outputFrame.frame = inputFrame.frame + "_output";
            outputFrame.number = inputFrame.number % 2;
            return outputFrame;
        }
    }


    public static void main(String[] args) throws Exception {

        String stream_video_name_1 = "video";

        // Configure network connection to NES REST server
        System.out.println(" *** connection:\n");
        NebulaStreamRuntime ner = NebulaStreamRuntime.getRuntime();
        ner.getConfig().setHost("192.168.7.40").setPort("8081");
        System.out.println(" *** connected:\n");

        videoFramesPreProcessingWithMap(ner, stream_video_name_1);

    }
}
