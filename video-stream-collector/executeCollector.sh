#!/bin/bash
function executeCollector(){
  mvn clean package exec:java -Dexec.mainClass="eu.ubitech.video.app.kafka.collector.VideoStreamCollector" -Dexec.cleanupDaemonThreads=false
}
executeCollector
