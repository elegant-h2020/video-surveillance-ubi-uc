package ocr;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.graph.ComputationGraph;
import stream.nebula.operators.sinks.FileSink;
import stream.nebula.runtime.NebulaStreamRuntime;
import stream.nebula.runtime.Query;
import stream.nebula.udf.MapFunction;

import static stream.nebula.expression.Expressions.attribute;

public class OcrElegantUseCase {
    private static void opticalCharacterRecognition(NebulaStreamRuntime ner, String stream_name) throws Exception {
        Query worker = ner.readFromSource(stream_name);

        // Project Operator chained with Map (Java UDF) operator
        worker.project(attribute("encodedInputImage"), attribute("realText"))
                .map(new InputImageMapper());

        // Sink Operator (FileSink)
        worker.sink(new FileSink("/output_opticalCharacterRecognition_query.csv", "CSV_FORMAT", true));

        // Get Query ID
        int queryId = ner.executeQuery(worker, "BottomUp");
        System.out.println("Query placed for execution with id: " + queryId);
        System.out.println(" *** \n");
    }

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

    public static void main(String[] args) throws Exception {
        String stream_name = "ocr";


        // Configure network connection to NES REST server
        System.out.println(" *** connection:\n");
        NebulaStreamRuntime ner = NebulaStreamRuntime.getRuntime();
        ner.getConfig().setHost("localhost").setPort("8081");
        System.out.println(" *** connected:\n");


        opticalCharacterRecognition(ner, stream_name);

    }
}
