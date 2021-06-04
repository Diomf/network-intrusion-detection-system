#!/bin/bash

set -a
KAFKA_TOPIC="netflows"
DATASET_LOCATION="/home/diomfeas/Desktop/Diplomatiki/Datasets/NSL-KDD"
MODELS_LOCATION="/home/diomfeas/Desktop/Models"
TRAINING_FILE="KDDTrain+.txt"
ML_ALGORITHM="rf" # rf for Random forest or dt for Decision Tree
OUTPUT_METHOD="elasticsearch" #console or elasticsearch
set +a
#export KAFKA_TOPIC DATASET_LOCATION MODELS_LOCATION TRAINING_FILE ML_ALGORITHM OUTPUT_METHOD
docker-compose -f docker-compose.yml up
