package org.neointegrations.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;

import javax.jms.Message;

public class JMSConsumer implements AutoCloseable {
    private final MessageConsumer consumer;
    private final Connection connection;
    private final Session session;

    private static final String queueName = JMSUtil.getProperties("destination.name");
    private static final boolean transactionEnabled = false;
    private static final int clientAckMode = Session.CLIENT_ACKNOWLEDGE;

    private static  final String userName = JMSUtil.getProperties("user.name");
    private static  final String password = JMSUtil.getProperties("password");
    private static final String jmsURL = JMSUtil.getProperties("jms.url");
    private static final int INITIAL_REDELIVERY_DELAY = 10 * 1000;
    private static final int REDELIVERY_DELAY = 10 * 1000;
    private static final int MAXIMUM_REDELIVERIES = 3;

    public JMSConsumer(Connection connection) throws JMSException {
        this.connection = connection;
        session = this.connection.createSession(transactionEnabled, clientAckMode);
        Destination destination = session.createQueue(queueName);
        consumer = session.createConsumer(destination);
    }

    public void receiveMessage() throws JMSException {
        Message message = consumer.receive();
        if (message instanceof TextMessage) {
            try {
                TextMessage text = (TextMessage) message;
                System.out.println("Message is : " + text.getText());
                if (transactionEnabled)
                    session.commit();
                else if (clientAckMode == Session.CLIENT_ACKNOWLEDGE)
                    message.acknowledge();
            } catch (JMSException exp) {
                if(transactionEnabled) session.rollback();
                else if(clientAckMode == Session.CLIENT_ACKNOWLEDGE) session.recover();
            }

        }
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

    @Override
    public void close() throws JMSException {
        consumer.close();
        session.close();
    }
    public static void main(String[] args) throws JMSException {
        Connection connection = connection();
        JMSConsumer consumer = new JMSConsumer(connection);

        consumer.receiveMessage();

        consumer.close();
        connection.stop();
        connection.close();
    }
}
