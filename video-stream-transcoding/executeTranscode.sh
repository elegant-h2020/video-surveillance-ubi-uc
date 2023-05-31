#!/bin/bash
function executetranscode(){
  mvn clean package exec:java -Dexec.mainClass="eu.ubitech.video.app.transcoding.driver.VideoTranscodingDriver" -Dexec.cleanupDaemonThreads=false
}
executetranscode
