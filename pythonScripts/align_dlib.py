#!/usr/bin/env python2
#
# Copyright 2015-2016 Carnegie Mellon University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import cv2
import numpy as np
import os
import random
import shutil
import sys

import openface
import openface.helper
from openface.data import iterImgs

import logging
# fileDir = os.path.dirname(os.path.realpath(__file__))
fileDir = "/root/openface"
modelDir = os.path.join(fileDir, 'models')
dlibModelDir = os.path.join(modelDir, 'dlib')
openfaceModelDir = os.path.join(modelDir, 'openface')

class AlignArguments():

    def __init__(self, inputDir, dlibFacePredictor=None):
        # Input image directory.
        self.inputDir = inputDir
        
        # Path to dlib's face predictor.
        if dlibFacePredictor==None:
            self.dlibFacePredictor = os.path.join(dlibModelDir, "shape_predictor_68_face_landmarks.dat")
        else:
            self.dlibFacePredictor = dlibFacePredictor

    def computeMeanArguments(self, numImages=0):
        self.mode = "computeMean"
        # The number of images. '0' for all images.
        # <= 0 ===> all imgs
        self.numImages = numImages

    def aligmentArguments(self, landmarks, outputDir, size=96, fallbackLfw=None, skipMulti=False, verbose=False):
        self.mode = "align"
        
        landmarksChoices=['outerEyesAndNose', 'innerEyesAndBottomLip','eyes_1']
        # The type of landmarks to use.
        if landmarks in landmarksChoices:
            self.landmarks = landmarks
        else:
            logging.error("There is no such landmarks choice")
            # print "There is no such landmarks choice"
            exit(-1)

        self.outputDir = outputDir
        self.size = size
        self.fallbackLfw = fallbackLfw
        self.skipMulti = skipMulti
        self.verbose = verbose

class AlignDlib():
    def __init__(self, args):
        self.args = args
    def write(vals, fName):
        if os.path.isfile(fName):
            logging.info(fName + " exists. Backing up.")
            # print("{} exists. Backing up.".format(fName))
            os.rename(fName, "{}.bak".format(fName))
        with open(fName, 'w') as f:
            for p in vals:
                f.write(",".join(str(x) for x in p))
                f.write("\n")

    def computeMeanMain(self):
        align = openface.AlignDlib(args.dlibFacePredictor)

        imgs = list(iterImgs(args.inputDir))
        if args.numImages > 0:
            imgs = random.sample(imgs, args.numImages)

        facePoints = []
        for img in imgs:
            rgb = img.getRGB()
            bb = align.getLargestFaceBoundingBox(rgb)
            alignedPoints = align.align(rgb, bb)
            if alignedPoints:
                facePoints.append(alignedPoints)

        facePointsNp = np.array(facePoints)
        mean = np.mean(facePointsNp, axis=0)
        std = np.std(facePointsNp, axis=0)

        write(mean, "{}/mean.csv".format(args.modelDir))
        write(std, "{}/std.csv".format(args.modelDir))

        # Only import in this mode.
        import matplotlib as mpl
        mpl.use('Agg')
        import matplotlib.pyplot as plt

        fig, ax = plt.subplots()
        ax.scatter(mean[:, 0], -mean[:, 1], color='k')
        ax.axis('equal')
        for i, p in enumerate(mean):
            ax.annotate(str(i), (p[0] + 0.005, -p[1] + 0.005), fontsize=8)
        plt.savefig("{}/mean.png".format(args.modelDir))

    def alignMain(self):
        openface.helper.mkdirP(self.args.outputDir)

        imgs = list(iterImgs(self.args.inputDir))

        # Shuffle so multiple versions can be run at once.
        random.shuffle(imgs)

        landmarkMap = {
            'outerEyesAndNose': openface.AlignDlib.OUTER_EYES_AND_NOSE,
            'innerEyesAndBottomLip': openface.AlignDlib.INNER_EYES_AND_BOTTOM_LIP
        }
        if self.args.landmarks not in landmarkMap:
            raise Exception("Landmarks unrecognized: {}".format(self.args.landmarks))

        landmarkIndices = landmarkMap[self.args.landmarks]

        align = openface.AlignDlib(self.args.dlibFacePredictor)

        sizeImg = len(imgs)
        iterator = 1
        nFallbacks = 0
        # print ""
        for imgObject in imgs:
            # sys.stdout.write("\033[F")
            # print "Images: " + str(iterator) + "/" + str(sizeImg)
            iterator += 1

            # print("=== {} ===".format(imgObject.path))
            outDir = os.path.join(self.args.outputDir, imgObject.cls)
            openface.helper.mkdirP(outDir)
            outputPrefix = os.path.join(outDir, imgObject.name)
            imgName = outputPrefix + ".png"

            if os.path.isfile(imgName):
                if self.args.verbose:
                    logging.info(str(imgObject.path) + " Already found, skipping.")
                    # print("  + Already found, skipping.")
            else:
                rgb = imgObject.getRGB()
                if rgb is None:
                    if self.args.verbose:
                        logging.info(str(imgObject.path) + " Unable to load.")
                        # print("  + Unable to load.")
                    outRgb = None
                else:
                    outRgb = align.align(self.args.size, rgb,
                                         landmarkIndices=landmarkIndices,
                                         skipMulti=self.args.skipMulti)
                    if outRgb is None and self.args.verbose:
                        logging.info(str(imgObject.path) + " Unable to align.")
                        # print("  + Unable to align.")

                if self.args.fallbackLfw and outRgb is None:
                    nFallbacks += 1
                    deepFunneled = "{}/{}.jpg".format(os.path.join(self.args.fallbackLfw,
                                                                   imgObject.cls),
                                                      imgObject.name)
                    shutil.copy(deepFunneled, "{}/{}.jpg".format(os.path.join(self.args.outputDir,
                                                                              imgObject.cls),
                                                                 imgObject.name))

                if outRgb is not None:
                    if self.args.verbose:
                        logging.info(str(imgObject.path) + " Writing aligned file to disk.")
                        # print("  + Writing aligned file to disk.")
                    outBgr = cv2.cvtColor(outRgb, cv2.COLOR_RGB2BGR)
                    cv2.imwrite(imgName, outBgr)

        if self.args.fallbackLfw:
            logging.info('nFallbacks: ' +  str(nFallbacks))
            # print('nFallbacks:', nFallbacks)

