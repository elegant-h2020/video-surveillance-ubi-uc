package eu.ubitech.video.app.flink.util;

import com.google.gson.Gson;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.log4j.Logger;
import org.opencv.core.Point;

import java.util.List;
/*
*  A flink filter to
*  filter out frames
*  that don't contain faces
 */
public class FaceDataFilter  implements FilterFunction<String> {
    private static final Logger log = Logger.getLogger(FaceDataFilter.class);

    @Override
    public boolean filter(String s) throws Exception {
        boolean hasFace ;
        Gson gson = new Gson();
        VideoEventStringProcessed data = gson.fromJson(s,VideoEventStringProcessed.class);
        List<Point> lista = data.getLista();
        //Thats unnecessary
        if ( lista == null || lista.size() == 0){
            log.info("no face");
            hasFace = false;
        }
        else {
            log.info("has face");
            hasFace = true;
        }
        return hasFace;
    }
}
