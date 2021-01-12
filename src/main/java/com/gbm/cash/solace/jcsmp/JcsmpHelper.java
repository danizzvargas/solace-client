package com.gbm.cash.solace.jcsmp;

import java.util.concurrent.CountDownLatch;

import com.gbm.cash.solace.SolaceBase;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcsmpHelper extends SolaceBase {
    private static final Logger log = LoggerFactory.getLogger(SolaceBase.class);

    public static JCSMPSession createSession() throws InvalidPropertiesException {
        final JCSMPProperties prop = new JCSMPProperties();

        prop.setProperty(JCSMPProperties.HOST, HOST);
        prop.setProperty(JCSMPProperties.USERNAME, USERNAME);
        prop.setProperty(JCSMPProperties.PASSWORD, PASSWORD);
        prop.setProperty(JCSMPProperties.VPN_NAME, VPN_NAME);
        prop.setProperty(JCSMPProperties.SSL_TRUST_STORE, SSL_TRUSTSTORE);
        prop.setProperty(JCSMPProperties.SSL_TRUST_STORE_PASSWORD, SSL_TRUSTSTORE_PASSWORD);
        prop.setProperty(JCSMPProperties.SSL_VALIDATE_CERTIFICATE, SSL_VALIDATE_CERTIFICATE);

        return JCSMPFactory.onlyInstance().createSession(prop);
    }

    public static XMLMessageProducer createProducer(JCSMPSession session) throws JCSMPException {
        return session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {

            @Override
            public void responseReceived(String messageID) {
                log.info("Producer received response for msg: {}", messageID);
            }

            @Override
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                log.error("Producer received error for msg ID: {}, timestamp: {}", messageID, timestamp, e);
            }
        });
    }

    public static XMLMessageConsumer createConsumer(JCSMPSession session, CountDownLatch latch) throws JCSMPException {
        return session.getMessageConsumer(new XMLMessageListener() {

            @Override
            public void onReceive(BytesXMLMessage msg) {
                if (msg instanceof TextMessage) {
                    log.info("TextMessage received: {}", ((TextMessage) msg).getText());
                } else {
                    log.info("Message received.");
                }
                log.info("Message Dump: {}", msg.dump());
                // latch.countDown(); // unblock main thread
            }

            @Override
            public void onException(JCSMPException e) {
                log.error("Consumer received exception", e);
                latch.countDown(); // unblock main thread
            }
        });
    }
}
