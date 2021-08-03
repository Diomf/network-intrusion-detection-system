# network-intrusion-detection-system

## Project Description

### Features

## System Architecture
![alt text](https://i.imgur.com/jkndEV3.png)


## Installation 
The only requirement for the deployment of the system locally or on a cluster is the installation of Docker on each node.

## How to use
### Local mode


### Cluster mode
For the automated deployment of the services on a cluster, we need a Docker Swarm setup.

1. Run `docker swarm init` on a manager node to start the swarm.
2. Run `docker swarm join` to join the swarm from the rest of the nodes as workers or managers using the necessary token.
3. Run `docker node update --label-add <label> <node>` to add the required labels on each node according to your need.
The labels are:
    * `role=master` for 
    * `role=worker` for
    * `role=esnode` for

