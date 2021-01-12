package com.gbm.cash.solace.jms;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

public class JmsPublisher {

    final String TOPIC_NAME = "T/GettingStarted/pubsub";

    public void run(String... args) throws Exception {
        String[] split = args[1].split("@");

        String host = args[0];
        String vpnName = split[1];
        String username = split[0];
        String password = args[2];

        System.out.printf("TopicPublisher is connecting to Solace messaging at %s...%n", host);

        // Programmatically create the connection factory using default settings
        SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setVPN(vpnName);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        // Create connection to the Solace router
        Connection connection = connectionFactory.createConnection();

        // Create a non-transacted, auto ACK session.
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        System.out.printf("Connected to the Solace Message VPN '%s' with client username '%s'.%n", vpnName,
                username);

        // Create the publishing topic programmatically
        Topic topic = session.createTopic(TOPIC_NAME);

        // Create the message producer for the created topic
        MessageProducer messageProducer = session.createProducer(topic);

        // Create the message
        TextMessage message = session.createTextMessage("Hello world!");

        System.out.printf("Sending message '%s' to topic '%s'...%n", message.getText(), topic.toString());

        // Send the message
        // NOTE: JMS Message Priority is not supported by the Solace Message Bus
        messageProducer.send(topic, message, DeliveryMode.NON_PERSISTENT,
                Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        System.out.println("Sent successfully. Exiting...");

        // Close everything in the order reversed from the opening order
        // NOTE: as the interfaces below extend AutoCloseable,
        // with them it's possible to use the "try-with-resources" Java statement
        // see details at https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        messageProducer.close();
        session.close();
        connection.close();
    }
}
