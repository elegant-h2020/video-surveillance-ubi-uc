#!/bin/bash
mvn clean package exec:java -Dexec.mainClass="eu.ubitech.video.app.flink.processor.VideoStreamProcessor" -Dexec.cleanupDaemonThreads=false


