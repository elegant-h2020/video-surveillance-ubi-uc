#!/bin/bash
mkdir  /tmp/processed-data
mkdir /tmp/frames
mkdir /tmp/similarity
mkdir /tmp/transcode
cp -r video-stream-collector/sample-video /tmp/
cp -r video-stream-similarity/src/main/resources/face /tmp/
cp -r video-stream-similarity/src/main/resources/images /tmp/
cp video-stream-processor/src/main/resources/haarcascade_frontalface_alt.xml /tmp/

#start Kafka locally
#docker run --rm -it  -p 2181:2181 -p 3030:3030 -p 8082:8082 -p 8085:8081 -p 8083:8083 -p 9092:9092 -p 9581:9581 -p 9582:9582 -p 9583:9583 -p 9584:9584 -e ADV_HOST=127.0.0.1 landoop/fast-data-dev:latest

