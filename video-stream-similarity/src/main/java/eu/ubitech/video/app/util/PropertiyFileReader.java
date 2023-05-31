package eu.ubitech.video.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

public class PropertiyFileReader {
    private static final Logger logger = Logger.getLogger(PropertiyFileReader.class);
    private  static Properties prop = new Properties();
    public static Properties readPropertyFile() throws  Exception {
        if (prop.isEmpty()) {
            InputStream input = PropertiyFileReader
                    .class.getClassLoader()
                    .getResourceAsStream("stream-collection.properties");
            try{
                prop.load(input);
            } catch (IOException ex){
                logger.error(ex);
                throw ex;
            }finally {
                if (input != null){
                    input.close();
                }
            }

        }
        return prop;
    }
}
