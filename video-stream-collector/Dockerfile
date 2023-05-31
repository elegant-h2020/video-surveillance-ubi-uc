FROM ubuntu:18.04
COPY install.sh install.sh
RUN chmod +x install.sh && sync && ./install.sh && rm install.sh
ARG JAR_FILE=target/video*.jar
COPY ${JAR_FILE} app.jar
COPY sample-video /tmp/sample-video/
ENTRYPOINT ["java","-jar","/app.jar"]
