FROM registry.ubitech.eu/maestro/surveillance-demo/opencv-java11:4.5.0
WORKDIR
COPY sample-video /tmp/
ARG JAR_FILE=target/video-stream-capture*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
