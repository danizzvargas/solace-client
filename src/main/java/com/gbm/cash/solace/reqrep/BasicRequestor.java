package com.gbm.cash.solace.reqrep;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.gbm.cash.solace.SolaceBase;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SupportedProperty;

public class BasicRequestor extends SolaceBase {

    public void run() throws Exception {
        System.out.printf("BasicRequestor is connecting to Solace messaging at %s...%n", HOST);

        // Programmatically create the connection factory using default settings
        SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
        connectionFactory.setHost(HOST);
        connectionFactory.setVPN(VPN_NAME);
        connectionFactory.setUsername(USERNAME);
        connectionFactory.setPassword(PASSWORD);

        // Create connection to the Solace router
        Connection connection = connectionFactory.createConnection();

        // Create a non-transacted, auto ACK session.
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        System.out.printf("Connected to the Solace Message VPN '%s' with client username '%s'.%n", VPN_NAME, USERNAME);

        // Create the request topic programmatically
        Topic requestTopic = session.createTopic(TOPIC_NAME);

        // Create the message producer for the created queue
        MessageProducer requestProducer = session.createProducer(requestTopic);

        // The response will be received on this temporary queue.
        TemporaryQueue replyToQueue = session.createTemporaryQueue();

        // Create consumer for receiving the request's reply
        MessageConsumer replyConsumer = session.createConsumer(replyToQueue);

        // Start receiving replies
        connection.start();

        // Create a request.
        TextMessage request = session.createTextMessage("Sample Request");
        // The application must put the destination of the reply in the replyTo field of the request
        request.setJMSReplyTo(replyToQueue);
        // The application must put a correlation ID in the request
        String correlationId = UUID.randomUUID().toString();
        request.setJMSCorrelationID(correlationId);

        System.out.printf("Sending request '%s' to topic '%s'...%n", request.getText(), requestTopic.toString());

        // Send the request
        requestProducer.send(requestTopic, request, DeliveryMode.NON_PERSISTENT,
                Message.DEFAULT_PRIORITY,
                Message.DEFAULT_TIME_TO_LIVE);

        System.out.println("Sent successfully. Waiting for reply...");

        // the main thread blocks at the next statement until a message received or the timeout occurs
        int timeout = 1000;

        Message reply = replyConsumer.receive(timeout);

        if (reply == null) {
            throw new Exception("Failed to receive a reply in " + timeout + " msecs");
        }

        // Process the reply
        if (reply.getJMSCorrelationID() == null) {
            throw new Exception(
                    "Received a reply message with no correlationID. This field is needed for a direct request.");
        }

        // Apache Qpid JMS prefixes correlation ID with string "ID:" so remove such prefix for interoperability
        if (!reply.getJMSCorrelationID().replaceAll("ID:", "").equals(correlationId)) {
            throw new Exception("Received invalid correlationID in reply message.");
        }

        if (reply instanceof TextMessage) {
            System.out.printf("TextMessage response received: '%s'%n", ((TextMessage) reply).getText());
            if (!reply.getBooleanProperty(SupportedProperty.SOLACE_JMS_PROP_IS_REPLY_MESSAGE)) {
                System.out.println("Warning: Received a reply message without the isReplyMsg flag set.");
            }
        } else {
            System.out.println("Message response received.");
        }

        System.out.printf("Message Content:%n%s%n", SolJmsUtility.dumpMessage(reply));

        connection.stop();
        // Close everything in the order reversed from the opening order
        // NOTE: as the interfaces below extend AutoCloseable,
        // with them it's possible to use the "try-with-resources" Java statement
        // see details at https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        replyConsumer.close();
        requestProducer.close();
        session.close();
        connection.close();
    }
}