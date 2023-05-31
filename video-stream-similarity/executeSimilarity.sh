#!/bin/bash
mvn clean package exec:java -Dexec.mainClass="eu.ubitech.video.app.similarity.processor.SimilarityProcessor" -Dexec.cleanupDaemonThreads=false
