## Autoscaling in Event Driven Architecture

### Introduction
Autoscaling is one of the best and probably most talked about features of Kubernetes. HPA or Horizontal Pod Autoscaler works best when dealing with REST API based services (or microservices) since increase in the number of incoming requests leads to increased consumption of memory and CPU. Kuberentes readily provides these metrics and is used by HPA to scale the pods up or down. 

However, when working with event driven applications traditional HPA using CPU and memory metrics to scale up and down might not work, since events are consumed synchronusly one after another. Which means it doesn't matter if there are 10 events or 100K events to be processed, the CPU and memory consumption remains approximately the same.
This is where KEDA (Kubernetes Event Driven Autoscaling) can help. It can provide metrics such as queue depth or consumer lag to the HPA which it can use to autoscale pods.

Let's see it in action. We will be using Apache Kafka for this sample as it is one of the fastest growing event streaming service.

### Let's Start
For this sample I am using Kuberenetes cluster that comes with Docker Desktop for windows. However if you have any other kubernetes cluster as well this should work.

1. First lets create a new namespace in Kubernetes. 
```shell
kubectl apply -f 00-namespace.yaml
``` 
This will create a new name namespace called 'keda' in your kubernetes cluster. We will deploy all our workloads to this namespace so that it is easier to clean up later.
To use 'keda' as the defaul namespace run below command
```shell
kubectl config set-context --current --namespace=keda
```
2. Since we are using Apache Kafka we need to depoy zookeeper which is used to keep track multiple kafka brokers. However, for this blog we will be using kafka with only one broker. We also need expose a service through which kafka will connect to zookeeper over port 2181. 
```shell
kubectl apply -f 01-zookeeper.yaml
```
3. Now we will deploy kafka with single broker setup. Kafka will be exposed over port 9092. This service can be used as bootstrap server url by consumers and producers.
```shell
kubectl apply -f 02-kafka.yaml
```
This kafka cluster has one topic with 3 partiton 'buy-icecream'
4. Let's now create a producer app. For this I have a simple spring boot application which can produce events to the topic. Let's build and deploy this appication.
```shell
cd sample-producer
mvn install -DskipTests
docker build -t sample-producer:1 .
cd ..
kubectl apply -f 03-sample-producer.yaml
```
This will expose a service of type Loadbalancer so that the endpoints exposed by this service can be accessed from the host machine over port 8098.
5. Next let's deploy a consumer which will consume events from the topic. I have added a processing time of 5 seconds for each event consumed.
```shell
cd sample-consumer
mvn install -DskipTests
docker build -t sample-consumer:1 .
cd ..
kubectl apply -f 04-sample-consumer.yaml
```
6. We will also delpoy kowl which is visualization tool for apache kafka. We can see events in the topic as well as number of consumer group, members in each group and total lag for each consumer.
We will also expose a service of type Loadbalancer so that it can be accessed from host machine over port 8080.
```
cd kowl
docker build -t common-kowl:1 . --build-arg CONF_KAFKA_BROKERS=kafka-service:9092
kubectl apply -f 06-kowl.yaml
cd ..
```

### Without KEDA
Now we are all setup to start some testing. 
Access http://localhost:8098/swagger-ui/#/controller execute /buy/ice-creams endpoint. This will start producing events at a regular interval of 5 seconds which mimics the secnario order being placed for ice-cream at regular intervals.

The events being produced can be visualized in kowl http://localhost:8080/kowl.

Now let's see what happens when more and more people wants to buy ice-creams. Execute '/summer/season' endpoint which will start producing events at more frequent intervals 1 seconds in this case and since the consumer still takes 5 seconds to process each events the lag of pending events will start increasing and this can be visulaized in kowl by going to 'Consumer Group' link from the left navigation pane.
Wait for few minutes and lag will keep on growing.

Now let's see how can KEDA can help and scale up pods to meet the increased demands of summer.

### With KEDA
Let's first deploy KEDA.
Keda can be deployedd in multiple ways. Refer [Keda Docs](https://keda.sh/docs/2.8/deploy/). I am using below command. 
```shell
kubectl apply -f keda-2.8.0.yaml
```
Now let's deploy keda scaled object. 
```
kubectl apply -f 05-keda-scalar.yaml
```
We have to specify below attributes 
1. deployment name, which it should auto scale in our case sample-consumer.
2. Polling interval, how frequently it should poll required metrics.
3. minimum and maximum replica count it should maintain.
We also need to specify trigger for the scaled object
1. type, since we using kafka it will be kafka.
2. bootstrap server url of the kafka brokes.
3. consumer group for which it needs to monitor the lag.
4. topic on which consumer group lag to be calculated.
5. lag threshold, average target value to trigger autoscaling.

Now since we have deployed kafka wait for few minutes and you can see the number of pods for sample-consumer increasing. This can also be verified from kowl by looking at the members of sample-consumer.
```shell
kubectl get pods
```

Now lets see what happens when the rate of new events goes down i.e. in our story very less people are buying ice-creams, maybe it is winter time. Execute '/winter/season' which will decrease the rate of incoming events in this case a new event will be produced every 7 seconds. Wait for some time and the number of pods for sample-consumer should start decreasing. This can also be verified from kowl.

### How did it work.
When we deployed Keda scaled Object it create an HPA. Verify this by executing below command
```shell
kubectl get hpa
```
It provides and external metrics to this HPA which then triggers autoscaling. Run kubectl describe command to see the events which triggers the scale up and down.
```shell
kubectl describe hpa [HPA name from above]
```

Keda can be used with lots of other triggers such as IBM MQ, Influx and Prometheus etc. It provides a great way to autoscale based on external metrics which impacts application performance.
</br></br>
### Thats it folks!!</br></br>
### Happy Coding!!
