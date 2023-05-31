package eu.ubitech.video.app.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.ubitech.video.app.similarity.processor.FaceProcessor;
import eu.ubitech.video.app.similarity.processor.FaceSimilarity;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.log4j.Logger;
import org.javatuples.Triplet;
import org.opencv.core.Mat;
import eu.ubitech.video.app.similarity.processor.SimilarityProcessor;



import java.util.List;

import static eu.ubitech.video.app.similarity.processor.SimilarityProcessor.faceSimilarity;

public class SimilarityProcess extends RichMapFunction<VideoEventStringProcessed, String> {
    private static final Logger logger = Logger.getLogger(SimilarityProcess.class);

    @Override
    public String map(VideoEventStringProcessed videoEventStringProcessed) throws Exception {
        logger.info("================================================");
        List<Mat> corpFaces = FaceProcessor.corpFaces(videoEventStringProcessed);
        VideoEventSimilarityData videoEventSimilarityData = getSimilarityData(videoEventStringProcessed);
        for (int i =0 ; i < corpFaces.size();i++){
            //Get Similarity
            SimilarityProcessor.saveImage(corpFaces.get(i),videoEventStringProcessed);
            Triplet<String, Double, List<String>> triplet = faceSimilarity.whoIs(corpFaces.get(i));
            String whoIsSimilar = triplet.getValue0();
            Double min_dist = triplet.getValue1();
           // List<String> names_list = triplet.getValue2();
            videoEventSimilarityData.similarFaces.add(whoIsSimilar);
            videoEventSimilarityData.dist_list.add(min_dist);
            //TODO:Unnessecery info, need to clean this
            //videoEventSimilarityData.entry_names.add(names_list);
        }

        Gson gson = new Gson();
        JsonObject obj = new JsonObject();

        //Json Object that will be send as message
        obj.addProperty("cameraId",videoEventSimilarityData.getCameraId());
        obj.addProperty("timestamp", videoEventSimilarityData.getTimestamp());
        obj.addProperty("rows", videoEventSimilarityData.getRows());
        obj.addProperty("cols", videoEventSimilarityData.getCols());
/*
        obj.addProperty("type", videoEventSimilarityData.getType());
*/
        //Not gonna use it in front end
        obj.add("lista", new Gson().toJsonTree(videoEventSimilarityData.getSimilarFaces()));
        obj.add("face_distance", new Gson().toJsonTree(videoEventSimilarityData.getDist_list()));
        obj.add("min distance", new Gson().toJsonTree(videoEventSimilarityData.getDist_list()));
        obj.add("entries", new Gson().toJsonTree(videoEventSimilarityData.getEntry_names()));
        String json = gson.toJson(obj);
        logger.info("json is " +json);
        logger.info("================================================");


        return json;
    }

    public VideoEventSimilarityData getSimilarityData(VideoEventStringProcessed vi){
        String cameraId = vi.getCameraId();
        int cols = vi.getCols();
        int rows = vi.getRows();
        String timestamp = vi.getTimestamp();
        int type = vi.getType();

        VideoEventSimilarityData  videoEventSimilarityData = new VideoEventSimilarityData(cameraId,
                timestamp,
                rows,
                cols,
                type);
        return videoEventSimilarityData ;
    }
}
