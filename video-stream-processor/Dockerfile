FROM registry.ubitech.eu/maestro/surveillance-demo/opencv-java11:4.5.0
WORKDIR /
COPY src/main/resources/haarcascade_frontalface_alt.xml /tmp/
ARG JAR_FILE=target/video*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir /tmp/frames
RUN mkdir /tmp/processed-data
ENTRYPOINT ["java","-jar","/app.jar"]
