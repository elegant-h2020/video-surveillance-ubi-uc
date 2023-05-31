package eu.ubitech.video.app.similarity.processor;
//Kudos to : https://github.com/PacktPublishing/Java-Machine-Learning-for-Computer-Vision/blob/master/FaceRecognition/src/main/java/ramo/klevis/ml/recogntion/face/FaceRecognition.java

import org.apache.log4j.Logger;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.graph.vertex.GraphVertex;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.javatuples.Triplet;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.opencv.core.Mat;



import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FaceSimilarity {

    private static final Logger log = Logger.getLogger(FaceSimilarity.class);
    private static final double THRESHOLD = 0.57;
    private FaceNetSmallV2Model faceNetSmallV2Model;
    private ComputationGraph computationGraph;
    private static final NativeImageLoader LOADER = new NativeImageLoader(96, 96, 3);

    public HashMap<String, INDArray> getMemberEncodingsMap() {
        return memberEncodingsMap;
    }

    private final HashMap<String, INDArray> memberEncodingsMap = new HashMap<>();

    public FaceSimilarity(FaceNetSmallV2Model faceNetSmallV2Model)  {
        this.faceNetSmallV2Model = faceNetSmallV2Model;
        try {
            this.computationGraph = faceNetSmallV2Model.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(computationGraph.summary());
    }

    public FaceSimilarity() {
    }


    private INDArray transpose(INDArray indArray1) {
        INDArray one = Nd4j.create(new int[]{1, 96, 96});
        one.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(2)));
        INDArray two = Nd4j.create(new int[]{1, 96, 96});
        two.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(1)));
        INDArray three = Nd4j.create(new int[]{1, 96, 96});
        three.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(0)));
        return Nd4j.concat(0, one, two, three).reshape(new int[]{1, 3, 96, 96});
    }

    private INDArray read(Mat imread) throws IOException {

        //From openCV_core Mat to JavaCPPMat
        org.bytedeco.javacpp.opencv_core.Mat img = new  org.bytedeco.javacpp.opencv_core.Mat((Pointer)null)
        {{ address = imread.getNativeObjAddr();}};
        //Use the nativeImageLoader to convert to numerical matrix
        NativeImageLoader nativeImageLoader = new NativeImageLoader(imread.height(), imread.width(), img.channels());
        INDArray indArray= nativeImageLoader.asMatrix(img);//put image into INDArray
        return transpose(indArray);
    }

    private INDArray read(String pathname) throws IOException {
        opencv_core.Mat imread = opencv_imgcodecs.imread(new File(pathname).getAbsolutePath(), 1);
        INDArray indArray = LOADER.asMatrix(imread);
        return transpose(indArray);
    }

    private INDArray forwardPass(INDArray indArray) {
        Map<String, INDArray> output = computationGraph.feedForward(indArray, false);
        GraphVertex embeddings = computationGraph.getVertex("encodings");
        INDArray dense = output.get("dense");
        embeddings.setInputs(dense);
        INDArray embeddingValues = embeddings.doForward(false, LayerWorkspaceMgr.builder().defaultNoWorkspace().build());
        log.debug("dense =                 " + dense);
        log.debug("encodingsValues =                 " + embeddingValues);
        return embeddingValues;
    }

    private double distance(INDArray a, INDArray b) {
        return a.distance2(b);
    }

    public void loadModel() throws Exception {
        faceNetSmallV2Model = new FaceNetSmallV2Model();
        computationGraph = faceNetSmallV2Model.init();
        log.info(computationGraph.summary());
    }

    public void registerNewMember(String memberId, String imagePath) throws IOException {
        INDArray read = read(imagePath);
        memberEncodingsMap.put(memberId, forwardPass(normalize(read)));
    }

    private static INDArray normalize(INDArray read) {
        return read.div(255.0);
    }

    public Triplet<String, Double, List<String>> whoIs(Mat image) throws IOException {
        INDArray read = read(image);
        INDArray encodings = forwardPass(normalize(read));
        double minDistance = Double.MAX_VALUE;
        String foundUser = "";
       List<Double> dist_list = new ArrayList<>();
       List<String> str_list = new ArrayList<>();
        for (Map.Entry<String, INDArray> entry : memberEncodingsMap.entrySet()) {
            INDArray value = entry.getValue();
            double distance = distance(value, encodings);
            log.info("distance of " + entry.getKey()  + " is " + distance + "\n");
            DecimalFormat df = new DecimalFormat("#.###");
            distance= Double.parseDouble(df.format(distance));
            dist_list.add(distance);
            str_list.add(entry.getKey());
            if (distance < minDistance) {
                minDistance = distance;
                foundUser = entry.getKey();
            }
        }
        if (minDistance > THRESHOLD) {
            foundUser = "Unknown user";
        }
        log.info(foundUser + " with distance " + minDistance + "\n");
        Triplet<String,Double,List<String>> triplet
                = //new Triplet<String,List<Double>,List<String>>(foundUser,dist_list,str_list);
                new Triplet<String,Double,List<String>>(foundUser,minDistance,str_list);

        return triplet;


    }
}