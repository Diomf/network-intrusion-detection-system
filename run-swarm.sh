#!/bin/bash
#
# Labels needed to be set on swarm nodes for the services:
#   - role=master --> Zookeeper, spark master and spark-submits
#   - role=worker --> Spark workers and kafka brokers
#   - role=esnode --> Elasticsearch nodes,kibana and elasticsearch-kibana setup service
#
#   e.g "docker node update --label-add role=master <Node-Id>
#

set -a
KAFKA_TOPIC="netflows"
NUM_OF_SPARK_WORKERS="2"
MODELS_LOCATION="s3a://minasdataset/Models"
TRAINING_FILE_LOCATION="s3a://minasdataset/KDDTrain+.txt"
SPARK_TRAIN_ARGUMENTS="--conf spark.hadoop.fs.s3a.endpoint=s3.eu-central-1.amazonaws.com --conf spark.hadoop.fs.s3a.access.key=AKIATHZAGHYTESFTZEO2 --conf spark.hadoop.fs.s3a.secret.key=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"
ML_ALGORITHM="rf" # rf for Random forest or dt for Decision Tree
OUTPUT_METHOD="elasticsearch" #console or elasticsearch
SPARK_TEST_ARGUMENTS="--conf spark.network.timeout=300 --conf spark.sql.streaming.metricsEnabled=true --conf spark.hadoop.fs.s3a.endpoint=s3.eu-central-1.amazonaws.com
 --conf spark.hadoop.fs.s3a.access.key=<access-key> --conf spark.hadoop.fs.s3a.secret.key=<secret-key>"
ELASTICSEARCH_NODE_NAMES="node6.swarm1.network-intrusion-detect-pg0.emulab.net,node7.swarm1.network-intrusion-detect-pg0.emulab.net,nodees.swarm1.network-intrusion-detect-pg0.emulab.net"
set +a

sudo docker stack deploy -f docker-compose-swarm.yml sparkstack
