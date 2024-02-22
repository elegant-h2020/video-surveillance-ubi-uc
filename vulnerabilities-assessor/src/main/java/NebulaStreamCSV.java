import stream.nebula.exceptions.RESTException;
import stream.nebula.operators.sinks.FileSink;
import stream.nebula.operators.sinks.Sink;
import stream.nebula.runtime.NebulaStreamRuntime;
import stream.nebula.runtime.Query;
import stream.nebula.udf.MapFunction;
import java.io.IOException;
import static stream.nebula.expression.Expressions.attribute;


public class NebulaStreamCSV {

    public static void main(String[] args) throws RESTException, IOException {
        // Create a NebulaStream runtime and connect it to the NebulaStream coordinator.
        NebulaStreamRuntime nebulaStreamRuntime = NebulaStreamRuntime.getRuntime();
        nebulaStreamRuntime.getConfig().setHost("localhost").setPort("8081");

        // Create a streaming query
        Query vulnerabilities2 = nebulaStreamRuntime
                .readFromSource("vulnerabilities")
                .project(attribute("accessvector"), attribute("name"),attribute("cvssversion"), attribute("attackvector"),attribute("cvssscore"), attribute("networktext"));

        // Vulnerabilities that have accessvector or attackvector "NETWORK" and CVSS version is V2 or supports V2 and V3
        vulnerabilities2.filter(
                (attribute("attackvector").equalTo(attribute("networktext"))

                        .or(attribute("accessvector").equalTo(attribute("networktext"))))
                        .and(attribute("cvssversion")
                                .equalTo(2)
                                .or(attribute("cvssversion")
                                        .equalTo(23))));




        vulnerabilities2.project(attribute("name"),attribute("cvssscore"));
        vulnerabilities2.map(new CvssScoreToSeverityV2());

        // Finish the query with a sink
        Sink result = vulnerabilities2.sink(new FileSink("/tutorial/cvssv2.csv", "CSV_FORMAT", false));

        int queryId = nebulaStreamRuntime.executeQuery(vulnerabilities2, "BottomUp");

        System.out.println("QueryW1 placed for execution with id: " + queryId);

        // Create a streaming query
        Query vulnerabilities3 = nebulaStreamRuntime
                .readFromSource("vulnerabilities")
                .project(attribute("accessvector"), attribute("name"),attribute("cvssversion"), attribute("attackvector"),attribute("cvssscore"), attribute("networktext"));


        vulnerabilities3.filter(
                (attribute("attackvector").equalTo(attribute("networktext"))

                        .or(attribute("accessvector").equalTo(attribute("networktext"))))
                        .and(attribute("cvssversion")
                                .equalTo(3)
                                .or(attribute("cvssversion")
                                        .equalTo(23))));


        // assign a severity category according to the vulnerability score
        vulnerabilities3.project(attribute("name"),attribute("cvssscore"));
        vulnerabilities3.map(new CvssScoreToSeverityV3());


        // Finish the query with a sink
        Sink result2 = vulnerabilities3.sink(new FileSink("/tutorial/cvssv3.csv", "CSV_FORMAT", false));

        // Submit the query to the coordinator.
        int queryId2 = nebulaStreamRuntime.executeQuery(vulnerabilities3, "BottomUp");

        System.out.println("QueryW1 placed for execution with id: " + queryId2);
    }

}


class CvssInput {
    String name;
    float cvssscore;
}

class CvssOutput {
    String name;
    String severity;
}

// This UDF determines the severity level based on the cvssscore
class CvssScoreToSeverityV2 implements MapFunction<CvssInput, CvssOutput> {
    public CvssOutput map(final CvssInput input) {
        CvssOutput output = new CvssOutput();
        output.name = input.name;
        if (input.cvssscore >= 0 && input.cvssscore <= 3.9) {
            output.severity = "LOW";
        } else if (input.cvssscore >= 4 && input.cvssscore <= 6.9) {
            output.severity = "MEDIUM";
        } else if (input.cvssscore >= 7 && input.cvssscore <= 10) {
            output.severity = "HIGH";
        } else {
            output.severity = "UNKNOWN"; // cvssscore is out of expected range
        }
        return output;
    }
}


class CvssScoreToSeveritySingleV2 implements MapFunction<Float, String> {
    public String map(final Float cvssscore) {
        if (cvssscore >= 0 && cvssscore <= 3.9) {
            return "LOW";
        } else if (cvssscore >= 4 && cvssscore <= 6.9) {
            return "MEDIUM";
        } else if (cvssscore >= 7 && cvssscore <= 10) {
            return "HIGH";
        } else {
            return "UNKNOWN";
        }
    }
}

class CvssScoreToSeverityV3 implements MapFunction<CvssInput, CvssOutput> {
    public CvssOutput map(final CvssInput input) {
        CvssOutput output = new CvssOutput();
        output.name = input.name;
        if (input.cvssscore >= 0.1 && input.cvssscore <= 3.9) {
            output.severity = "LOW";
        } else if (input.cvssscore >= 4 && input.cvssscore <= 6.9) {
            output.severity = "MEDIUM";
        } else if (input.cvssscore >= 7 && input.cvssscore <= 8.9) {
            output.severity = "HIGH";
        } else if (input.cvssscore >= 9 && input.cvssscore <= 10){
            output.severity = "CRITICAL";
        }else {
            output.severity = "UNKNOWN";
        }
        return output;
    }
}


class CvssScoreToSeveritySingleV3 implements MapFunction<Float, String> {
    public String map(final Float cvssscore) {
        if (cvssscore >= 0.1 && cvssscore <= 3.9) {
            return "LOW";
        } else if (cvssscore >= 4 && cvssscore <= 6.9) {
            return "MEDIUM";
        } else if (cvssscore >= 7 && cvssscore <= 8.9) {
            return "HIGH";
        } else if (cvssscore >= 9 && cvssscore <= 10){
            return "CRITICAL";
        } else {
            return "UNKNOWN";
        }
    }
}