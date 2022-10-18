FROM openjdk:17

RUN mkdir -p /exec

COPY target/sample-producer-0.0.1-SNAPSHOT.jar /exec/sample-producer-0.0.1-SNAPSHOT.jar

ENTRYPOINT [ "java", "-jar", "/exec/sample-producer-0.0.1-SNAPSHOT.jar" ]