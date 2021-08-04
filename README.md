# network-intrusion-detection-system

## Project Description
With the growing use of web based services, the network and complexity of network traffic data have increased dramatically as well. To tackle this issue, we developed a distributed real-time network intrusion detection system that utilizes supervised machine learning classifiers, and more specifically Decision Tree, Random Forest and Extreme Gradient Boosting, to distinguish between normal traffic and malicious attempts. After a training phase, unclassified log data is being processed in real-time through an Apache Kafka - Apache Spark(Structured streaming) pipeline and the classified flows are stored in Elasticsearch and displayed in a pre-designed dashboard on Kibana. All the required components are containerized using Docker to take advantage of its virtualization features and provide ease of deployment and scalability. The implementation of the system was based on the popular NSL-KDD network dataset.

### Features

   * **High performance**: Thanks to the scalability of the framework, it is fitted to withstand and successfully process heavy network traffic load. Its distributed nature enables computationally intensive analyses.
   * **Easy deployment**: The deployment of the framework is fully automated for deployment using Docker for containerization and orchestration.
   * **Real-time analyses**: The stream-based approach provides results of network flow analysis with only a few seconds delay. The results can be explored in various ways in a Kibana user interface in real time.

## System Architecture
![alt text](https://i.imgur.com/jkndEV3.png)


## Installation 
The only requirement for the deployment of the system locally or on a cluster is the installation of Docker on each node.

## How to use
The NSL-KDD dataset, based on which this project has been developed, can be downloaded from https://www.kaggle.com/hassan06/nslkdd.

### Local mode
In order to run the project in local mode, simply configure the run.sh parameters according to your needs and run the script.
Docker-Compose will launch all the services on your machine along with the training and real-time classification phase.
Classified flows and metrics can be viewed on the kibana dashboard on <localhost:5601>.

The testing csv files can be streamed to the Kafka topic with the use of the python scripts and a kafka-console producer. Command example:
`python sendTestByRate.py 1000 | kafka-console-producer.sh --bootstrap-server <broker_ip>:9094 --topic netflows`

### Cluster mode
For the automated deployment of the services on a cluster, we need a Docker Swarm setup.

1. Run `docker swarm init` on a manager node to start the swarm.
2. Run `docker swarm join` to join the swarm from the rest of the nodes as workers or managers using the necessary token.
3. Run `docker node update --label-add <label> <node>` to add the required labels on each node based on your choices.
The labels are:
    * `role=master` for Zookeeper, Spark master nodes and nodes that submit the Spark jobs
    * `role=worker` for Spark workers and Kafka brokers
    * `role=esnode` for Elasticsearch nodes, Kibana and Elasticsearch-Kibana setup service
4. Configure the parameters in the run-swarm script according to your needs.
5. Run the run-swarm script from the manager node.

The testing csv files can be streamed to the Kafka topic with the use of the python scripts and a kafka-console producer. Command example:
`python sendTestByRate.py 1000 | kafka-console-producer.sh --bootstrap-server <broker_ip>:9094 --topic netflows`

The services will be deployed to the corresponding cluster nodes and the training phase will begin. After the training phase, the real-time classification phase starts and results can be viewed live in the Kibana dashboard at the <node_ip:5601> where node_ip is the ip of the node on which kibana has been deployed.

