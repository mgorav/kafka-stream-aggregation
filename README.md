## What is this app?

Spring Cloud Stream based aggregator processor which aggregates DomainEvent (eventType+boardUuid) i.e. aggregates a string for a particular key


## Running the app:


````bash
docker-compose up -d
````

```bash
./mvnw clean package
```

````bash
java -jar target/kafka-streams-aggregate-0.0.1-SNAPSHOT.jar
````


Produce a sample data using Producers application few times.

Play time, hit the URL: 
```bash
http://localhost:8080/events
```
