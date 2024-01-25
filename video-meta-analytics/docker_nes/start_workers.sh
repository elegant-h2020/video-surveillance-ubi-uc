#!/bin/bash

#TODO: use cnitdata instead of exdratest*

#Why I can use different 127.x.x.x IPs without having active interfaces? Here: https://networkengineering.stackexchange.com/a/44636

set -e

if [ $# -eq 0 ]
then
    /opt/local/nebula-stream/nesWorker \
        --coordinatorPort=$coordinatorPort \
        --localWorkerIp="127.0.0.2" \
        --physicalSources.type=$sourceType \
        --physicalSources.filePath=/opt/local/nebula-stream/transc_data_1.csv \
        --physicalSources.numberOfBuffersToProduce=0 \
        --physicalSources.sourceFrequency=0 \
        --physicalSources.physicalSourceName=nes_worker1 \
        --physicalSources.logicalSourceName=trasnc_stream\
        --skipHeader=$skipHeader \
    > worker1.log &
    sleep 5s
    /opt/local/nebula-stream/nesWorker \
        --coordinatorPort=$coordinatorPort \
        --localWorkerIp="127.0.0.3" \
        --physicalSources.type=$sourceType \
        --physicalSources.filePath=/opt/local/nebula-stream/transc_data_2.csv \
        --physicalSources.numberOfBuffersToProduce=0 \
        --physicalSources.sourceFrequency=0 \
        --physicalSources.physicalSourceName=nes_worker2 \
        --physicalSources.logicalSourceName=transc_stream\
        --physicalSources.skipHeader=$skipHeader \
    > worker2.log &
else
    exec $@
fi
