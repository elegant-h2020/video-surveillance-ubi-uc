#!/bin/bash

apt-get update 

apt-get install -y openjdk-11-jdk

apt-get install -y curl

apt-get install -y ant

apt-get install -y cmake

apt-get install -y g++

apt-get install -y tree 

apt-get install -y build-essential cmake git pkg-config libgtk-3-dev \
    libavcodec-dev libavformat-dev libswscale-dev libv4l-dev \
    libxvidcore-dev libx264-dev libjpeg-dev libpng-dev libtiff-dev \
    gfortran openexr libatlas-base-dev python3-dev python3-numpy \
    libtbb2 libtbb-dev libdc1394-22-dev


export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"

export ANT_HOME="/usr/bin/ant"

curl -sL https://github.com/opencv/opencv/archive/refs/tags/4.5.0.tar.gz | tar xvz -C /tmp

cd /tmp/opencv-4.5.0 && mkdir build && cd build

cmake -D BUILD_SHARED_LIBS=OFF ..

make -j8

make install

mkdir /opencv-java-bin

cp bin/opencv-450.jar lib/libopencv_java450.so /opencv-java-bin

cp lib/libopencv_java450.so /usr/lib


cp bin/opencv-450.jar /usr/local/bin

rm -rf /var/lib/apt/lists/* /tmp/*opencv* /var/tmp/*




