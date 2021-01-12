package com.gbm.cash.solace;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

import javax.jms.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class SolaceClientJMS {
    public static void main(String[] args) throws Exception {
        System.out.println("====== JMS client ======");

        SolaceConnector connector = new SolaceConnector();

        System.out.println("Connecting");
        connector.createSession();

        if (args.length > 0) {
            System.out.println(String.format("Input: %s", args[0]));

            // CONSUMER
            if (args[0].equals("c") || args[0].equals("consumer")) {
                connector.run();
            }

            // PRODUCER
            else if (args[0].equals("p") || args[0].equals("producer")) {
                connector.sendMessage();
            }

            // NO OPTION SELECTED
            else {
                System.out.println("No valid option selected. Use 'c' for consumer or 'p' for producer.");
            }
        }

        System.out.println("Done");
    }
}

class SolaceConnector implements Runnable {
    private static final Properties prop = new Properties();
    private final CountDownLatch latch = new CountDownLatch(1);
    private Session session;
    private Connection connection;

    static {
        try (InputStream resource = SolaceConnector.class.getClassLoader().getResourceAsStream("application.properties")) {
            // Load properties.
            prop.load(resource);

            // Override properties.
            for (Object key : prop.keySet()) {
                String override = System.getProperty((String) key);
                if (override != null) {
                    prop.put(key, override);
                }
            }

        } catch (IOException ex) {
            System.err.println("Error trying to retrieve properties.");
            ex.printStackTrace();
        }
    }

    public void sendMessage() throws JMSException {
        System.out.println("Producer selected");

        String topicName = prop.getProperty("solace.topic_name");
        Topic topic = session.createTopic(topicName);

        MessageProducer messageProducer = session.createProducer(topic);

        TextMessage message = session.createTextMessage("Hello world!");
        messageProducer.send(topic, message,
                DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);

        System.out.println("Sent successfully. Exiting...");

        // Close everything in the order reversed from the opening order
        // NOTE: as the interfaces below extend AutoCloseable,
        // with them it's possible to use the "try-with-resources" Java statement
        // see details at https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        messageProducer.close();
        session.close();
        connection.close();
    }

    public void createSession() throws Exception {
        String host = prop.getProperty("solace.host");
        String username = prop.getProperty("solace.username");
        String vpnName = prop.getProperty("solace.vpn_name");
        String password = prop.getProperty("solace.password");
        String topicName = prop.getProperty("solace.topic_name");

        System.out.println("Solace host: " + host);
        System.out.println("Solace username: " + username);
        System.out.println("Solace VPN name: " + vpnName);
        System.out.println("Solace topic name: " + topicName);

        SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();

        connectionFactory.setHost(host);
        connectionFactory.setVPN(vpnName);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Override
    public void run() {
        try {
            String topicName = prop.getProperty("solace.topic_name");
            Topic topic = session.createTopic(topicName);

            // Create the message consumer for the subscription topic
            MessageConsumer messageConsumer = session.createConsumer(topic);

            messageConsumer.setMessageListener(message -> {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
