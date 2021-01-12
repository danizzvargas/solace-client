package com.gbm.cash.solace.jms;

import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

public class JmsSubscriber {

    final String TOPIC_NAME = "T/GettingStarted/pubsub";

    // Latch used for synchronizing between threads
    final CountDownLatch latch = new CountDownLatch(1);

    public void run(String... args) throws Exception {
        String[] split = args[1].split("@");

        String host = args[0];
        String vpnName = split[1];
        String username = split[0];
        String password = args[2];

        System.out.printf("TopicSubscriber is connecting to Solace messaging at %s...%n", host);

        // Programmatically create the connection factory using default settings
        SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setVPN(vpnName);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        Connection connection = connectionFactory.createConnection();

        // Create a non-transacted, Auto ACK session.
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        System.out.printf("Connected to Solace Message VPN '%s' with client username '%s'.%n", vpnName,
                username);

        // Create the subscription topic programmatically
        Topic topic = session.createTopic(TOPIC_NAME);

        // Create the message consumer for the subscription topic
        MessageConsumer messageConsumer = session.createConsumer(topic);

        // Use the anonymous inner class for receiving messages asynchronously
        messageConsumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage) {
                        System.out.printf("TextMessage received: '%s'%n", ((TextMessage) message).getText());
                    } else {
                        System.out.println("Message received.");
                    }
                    System.out.printf("Message Content:%n%s%n", SolJmsUtility.dumpMessage(message));
                    latch.countDown(); // unblock the main thread
                } catch (JMSException ex) {
                    System.out.println("Error processing incoming message.");
                    ex.printStackTrace();
                }
            }
        });

        // Start receiving messages
        connection.start();
        System.out.println("Awaiting message...");
        // the main thread blocks at the next statement until a message received
        latch.await();

        connection.stop();
        // Close everything in the order reversed from the opening order
        // NOTE: as the interfaces below extend AutoCloseable,
        // with them it's possible to use the "try-with-resources" Java statement
        // see details at https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        messageConsumer.close();
        session.close();
        connection.close();
    }
}
