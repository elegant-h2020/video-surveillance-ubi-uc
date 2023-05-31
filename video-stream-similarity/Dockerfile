FROM registry.ubitech.eu/maestro/surveillance-demo/opencv-java11:4.5.0
WORKDIR /
ARG JAR_FILE=target/video-stream-similarity-1.0.0.jar
COPY ${JAR_FILE} app.jar
RUN mkdir /tmp/similarity
RUN mkdir /tmp/face
RUN mkdir /tmp/images
COPY src/main/resources/face /tmp/face
COPY src/main/resources/images /tmp/images
ENTRYPOINT ["java","-jar","/app.jar"]
