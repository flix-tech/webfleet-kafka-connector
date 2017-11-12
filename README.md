# Webfleet Kafka Connector

## Introduction
This connector consumes messages from webfleet API and publish to a Kafka topic.
Messages are produced using Schema of webfleet (Schema Registry is required).
You can check `com.flixtech.kafka.WebfleetSchema` for more details on the Webfleet message Schema.  

## Parameters to be passed while starting the Webfleet Kafka connector:
 ### Basic required parameters:
 1. topic: Output topic of Kafka (Sink)
 2. webfleet.api.endpoint_url: URL to connect to webfleet api
 3. webfleet.api.account: Account name to connect to webfleet api
 4. webfleet.api.user: User name to connect to webfleet api
 
 ### Password related parameters:
 We provide implementation to use `credstash` for retrieving api password or
 adding password directly as a parameter. Only one of the below options should 
 be used:
 
 #### Credstash parameters:
 1. webfleet.api.password.credstash_table: Credstash table where the key is stored
 2. webfleet.api.password.credstash_key: Credstash secret key

 #### Password as a parameter:
 1. webfleet.api.password: Password to connecto to webfleet api. 


## Example:
```
curl -i -X POST \
           -H "Accept:application/json" \
           -H "Content-Type:application/json" \
           localhost:8083/connectors/ \
           -d '{
				"name": "<NAME OF CONNECTOR>",
  				"config": {
				    "connector.class": "com.flixtech.kafka.WebfleetSourceConnector",
				    "tasks.max": "1",
				    "topic": "<Output Kafka Topic>",
				    "webfleet.api.endpoint_url": "<ENDPOINT URL>",
				    "webfleet.api.account": "<ACCOUNT NAME>",
				    "webfleet.api.user": "<USER NAME>",
				    "webfleet.api.password": "<PASSWORD>"
  				}
  			}'

```

### Note:
 1. Webfleet API after each pull requires to be acknowledged per user. 
    Thus having multiple tasks does not inherently make sense. 
    Thus `task.max` is always defaulted to `1`.
    
 2. Current implementation only support one user. If you want to use multiple user on multiple queues 
    you can start multiple connectors. 
 
 3. Webfleet limits the number of pull and acknowledgment you can send per minute. 
    Generally this limit is 10 per minute.
