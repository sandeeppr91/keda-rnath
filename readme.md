## Autoscaling in Event Driven Architure(Kafka)
This repository is used for PoC of event driven scaling in K8s.

### Prerequisites
1. Docker and Kubernetes setup.
   If using windows latest version of [Docker Desktop](https://www.docker.com/products/docker-desktop/) can be be installed which comes Kubernetes.
2. kubectl   
3. Maven and Java 17 for building the sample-producer and sample-consumer app.   
4. helm cli for kafka-lag-exporter

### Resources Used
1. Zookeeper And Kafka as messaging service
2. A producer app which will produce messages to a topic in Kafka. The rate at which producer produces messages can be changed.
3. A consumer app which will consume messages from the same topic. Consumer consumes message at a specified rate.
4. Kowl for visualizing data in Kafka.
5. Keda for event driven scaling.

### Steps to create resources
In cli run the below commands.
1. Change directory to [sample-producer](./sample-producer) and follow below steps
```shell
cd sample-producer
mvn install -DskipTests
Run docker build -t sample-prducer:1 .
cd ..
```
2. Change directory to [sample-consumer](./sample-consumer)
```shell
cd sample-consumer
mvn install -DskipTests
Run docker build -t sample-consumer:1 .
cd ..
```
3. Change directory back to root level.
4. Execute below command to create a namespace in kubernetes.
```shell
kubectl apply -f 00-namespace.yaml
```
5. Execute below command to start zookeeper service.
```shell
kubectl apply -f 01-zookeeper.yaml
```
6. Execute below command to create Kafka instance.
```shell
kubectl apply -f 02-kafka.yaml
```
7. Execute below command to create sample producer instance.
```shell
kubectl apply -f 03-sample-producer.yaml
```
8. Execute below command to create sample consumer instance.
```shell
kubectl apply -f 04-sample-consumer.yaml
```
9. Execute below command to create scaler for sample consumer which will scale up and down based on consumer group lag.
```shell
kubectl apply -f 05-keda-scalar.yaml
```
10. Change directory to kowl and execute below script
```shell
docker build -t common-kowl:1 . --build-arg CONF_KAFKA_BROKERS=kafka-service:9092
```
11. Execute below command to create kowl instance.
```shell
kubectl apply -f 06-kowl.yaml
```

### Keda in Action
Kowl will be deployed on http://localhost:8080/kowl. You should be able to see a topic buy-icecream with 3 partitions.
![kowl.png](./blob/kowl.png)

Navigate to http://localhost:8098/swagger-ui/#/controller and hit ​/buy/ice-creams which will start producing messages to the topic in a frequent manner.
![swagger.png](./blob/swagger.png)

Execute below command you should see only one pod of sample-consumer.
```shell
kubectl get pods -n keda
```

To tigger Keda in action hit /summer/season in the swagger ui. This will increase the frequency of messages produced.

Wait for couple of minutes and rerun kubectl get pods -n keda you should be able to see more than one pod for sample-consumer.

In kowl navigate to consumer groups and click on sample-consumer the members should align with the number of pods for sample-consumer.


### Known Issues
If buy-icreams topic is created only with one partition run below command to fix the issue.
```shell
kubectl delete -f 01-zookeeper.yaml
kubectl delete -f 02-kafka.yaml
kubectl apply -f 01-zookeeper.yaml
kubectl apply -f 02-kafka.yaml
```

