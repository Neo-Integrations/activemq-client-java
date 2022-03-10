package org.neointegrations.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;

public class JMSProducer implements AutoCloseable {

    private final MessageProducer producer;
    private final Connection connection;
    private final Session session;

    private static final String virtualQueueName = JMSUtil.getProperties("destination.name");
    private static final boolean transactionEnabled = false;
    private static final int clientAckMode = Session.CLIENT_ACKNOWLEDGE;

    private static  final String userName = JMSUtil.getProperties("user.name");
    private static  final String password = JMSUtil.getProperties("password");
    private static final String jmsURL = JMSUtil.getProperties("jms.url");
    private static final int INITIAL_REDELIVERY_DELAY = 10 * 1000;
    private static final int REDELIVERY_DELAY = 10 * 1000;
    private static final int MAXIMUM_REDELIVERIES = 3;

    public JMSProducer(Connection connection) throws JMSException {
        this.connection = connection;
        session = this.connection.createSession(transactionEnabled, clientAckMode);
        Destination destination = session.createQueue(virtualQueueName);
        producer = session.createProducer(destination);
    }

    public void sendMessage(String msg) throws JMSException {
        TextMessage message = session.createTextMessage();
        message.setText(msg);
        producer.send(message);
        System.out.println("Sent: " + message.getText());

        if(transactionEnabled) session.commit();
        else if(clientAckMode == Session.CLIENT_ACKNOWLEDGE) message.acknowledge();

    }


    @Override
    public void close() throws JMSException {
        producer.close();
        session.close();
    }


    private static Connection connection() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(jmsURL);

        RedeliveryPolicy policy = new RedeliveryPolicy();
        policy.setInitialRedeliveryDelay(INITIAL_REDELIVERY_DELAY);
        policy.setRedeliveryDelay(REDELIVERY_DELAY);
        policy.setMaximumRedeliveries(MAXIMUM_REDELIVERIES);
        factory.setRedeliveryPolicy(policy);

        Connection connection = factory.createConnection(userName, password);
        connection.start();
        return connection;
    }

    public static void main(String[] args) throws JMSException {
        Connection connection = connection();
        JMSProducer sender = new JMSProducer(connection);

        for(int i = 1; i <= 10; i++) {
            sender.sendMessage("Hello World: "+ i);
        }

        sender.close();
        connection.stop();
        connection.close();

    }

}

