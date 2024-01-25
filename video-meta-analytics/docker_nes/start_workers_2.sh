#!/bin/bash

#TODO: use cnitdata instead of exdratest*

#Why I can use different 127.x.x.x IPs without having active interfaces? Here: https://networkengineering.stackexchange.com/a/44636

set -e

if [ $# -eq 0 ]
then

 nesWorker \
      --coordinatorPort=12346 \
      --localWorkerIp="127.0.0.3" \
      --physicalSources.type="CSVSource" \
      --physicalSources.filePath=/opt/local/nebula-stream/transc_data_2.csv \
      --physicalSources.numberOfBuffersToProduce=900 \
      --physicalSources.sourceFrequency=45000 \
      --physicalSources.physicalSourceName=nes_worker2 \
      --physicalSources.logicalSourceName=Transc_stream \
     0 --physicalSources.skipHeader=true
else
    exec $@
fi
