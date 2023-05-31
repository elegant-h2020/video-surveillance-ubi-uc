package eu.ubitech.video.app.flink.util;

import org.opencv.core.Point;

import java.util.LinkedList;
import java.util.List;


public class VideoEventStringProcessed  {
    private List<Point> lista ;
    private String cameraId;
    private String timestamp;
    private int rows;
    private int cols;
    private int type;
    private String data;



    public List<Point> getLista() {
        return lista;
    }

    public void setLista(List<Point> lista) {
        this.lista = lista;
    }

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


    public VideoEventStringProcessed(String cameraId, String timestamp ,int rows, int cols, int type, String data, List<Point> lista3) {
        this.cameraId = cameraId;
        this.timestamp=timestamp;
        this.rows=rows;
        this.cols=cols;
        this.type=type;
        this.data=data;
        this.lista= new LinkedList<Point>();
        this.lista.addAll(lista3);
    }

}
