## User Guide
Steps: 
-  Create a property file called `config.properties` in the current directory and place following values:
```shell
destination.name=salesforce_motorist_queue
user.name=admin
password=admin
jms.url=tcp://localhost:61616
```

- Place the attached `activemq-client-java-1.0-SNAPSHOT.jar` jar file in the current directory.

- Run the Producer: `java -cp activemq-client-java-1.0-SNAPSHOT.jar org.neointegrations.jms.JMSProducer`

- Run the Consumer: `java -cp activemq-client-java-1.0-SNAPSHOT.jar org.neointegrations.jms.JMSConsumer`

- Run the Listener: `java -cp activemq-client-java-1.0-SNAPSHOT.jar org.neointegrations.jms.JMSListener`