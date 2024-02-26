package eu.ubitech.video.app.flink.util;

import org.opencv.core.Point;

import java.util.LinkedList;
import java.util.List;

/**
 * Class containing all the information
 * of frame processed anlong with the
 * encoded image in base64
 */
public class VideoEventStringProcessed  {
    //    private List<Point> lista ;
    String data;
    String cameraId;
    String timestamp;
    int rows;
    int cols;
    int type;

//    public List<Point> getLista() {
//        return lista;
//    }
//
//    public void setLista(List<Point> lista) {
//        this.lista = lista;
//    }

    public String getCameraId() {
        return cameraId;
    }
    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public int getRows() {
        return rows;
    }
    public void setRows(int rows) {
        this.rows = rows;
    }
    public int getCols() {
        return cols;
    }
    public void setCols(int cols) {
        this.cols = cols;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Create the class conaining all metadata-info
     * @param cameraId
     * @param timestamp
     * @param rows number of rows
     * @param cols number of cols in image
     * @param type colorspace
     * @param data encoded (base64) image in string format
    //     * @param lista3 list of points needed to locate a face (top-right and bottom-left)
     */
    public VideoEventStringProcessed(String cameraId, String timestamp ,int rows, int cols, int type, String data) {
        this.cameraId = cameraId;
        this.timestamp=timestamp;
        this.rows=rows;
        this.cols=cols;
        this.type=type;
        this.data=data;
//        this.lista= new LinkedList<Point>();
//        this.lista.addAll(lista3);
    }

    public VideoEventStringProcessed() {
    }
}