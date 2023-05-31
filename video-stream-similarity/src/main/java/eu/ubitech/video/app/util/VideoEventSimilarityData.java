package eu.ubitech.video.app.util;

import java.util.ArrayList;
import java.util.List;

public class VideoEventSimilarityData {
    private String cameraId;
    private String timestamp;
    private int rows;
    private int cols;
    /*private int type;*/
    public List<String> similarFaces = new ArrayList<>();
    public List<List<String>> entry_names = new ArrayList<List<String>>();
    public List<Double> dist_list = new ArrayList();


    public List<List<String>> getEntry_names() {
        return entry_names;
    }

    public void setEntry_names(List<List<String>> entry_names) {
        this.entry_names = entry_names;
    }

    public List<Double> getDist_list() {
        return dist_list;
    }

    public void setDist_list(List<Double> dist_list) {
        this.dist_list = dist_list;
    }

    public VideoEventSimilarityData(String cameraId, String timestamp, int rows, int cols, int type) {
        this.cameraId = cameraId;
        this.timestamp = timestamp;
        this.rows = rows;
        this.cols = cols;
        /*this.type = type;*/
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


    public List<String> getSimilarFaces() {
        return similarFaces;
    }

    public void setSimilarFaces(List<String> similarFaces) {
        this.similarFaces = similarFaces;
    }
}
