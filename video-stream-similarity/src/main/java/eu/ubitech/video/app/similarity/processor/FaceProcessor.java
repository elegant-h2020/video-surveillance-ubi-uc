package eu.ubitech.video.app.similarity.processor;

import eu.ubitech.video.app.util.VideoEventStringProcessed;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.*;

public class FaceProcessor {
    private static final Logger log = Logger.getLogger(FaceProcessor.class);

    public static List<Mat> corpFaces(VideoEventStringProcessed vesp) throws Exception {
        // 2 Points -> 1 Face
        int facesInFrame = vesp.getLista().size()/2;
        Queue<Point> queue = new LinkedList<>(vesp.getLista());
        List<Mat> list_of_faces = new ArrayList<Mat>();
        Point bottomLeft ;
        Point topRight ;
        int width ;
        int height ;
        Mat frame = getMat(vesp);
        for(int i =0 ; i<facesInFrame; i++){
//            log.info("Faces in frame : " +facesInFrame + "\n");
//            log.info("Queue.list " + queue.size() + "\n");
            bottomLeft = queue.remove();
            topRight = queue.remove();
            width =(int)(topRight.x-bottomLeft.x) ;
            height = (int)(topRight.y -bottomLeft.y);
            Rect rectCrop = new Rect((int)(bottomLeft.x), (int)(bottomLeft.y), (int)(width), (int)(height));
            Mat image_roi = new Mat(frame,rectCrop);
            list_of_faces.add(image_roi);
        }
        return list_of_faces;
    }

    private static Mat getMat(VideoEventStringProcessed ed) throws Exception {
        /*
         * The mat object needs to match
         * the rows and cols of indarray of network
         */
        Mat mat = new Mat(ed.getRows(), ed.getCols(), ed.getType());
        mat.put(0, 0, Base64.getDecoder().decode(ed.getData()));
        return mat;
    }
}
