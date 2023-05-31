FROM registry.ubitech.eu/maestro/surveillance-demo/opencv-java11:4.5.0
WORKDIR /
ARG JAR_FILE=target/video-stream-transcoding*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir /tmp/transcode
ENTRYPOINT ["java","-jar","/app.jar"]
