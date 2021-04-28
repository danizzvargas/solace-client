package dzz.solace.reqrep;

import java.util.concurrent.CountDownLatch;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.solacesystems.jms.SupportedProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dzz.solace.SolaceBase;
import dzz.solace.reqrep.dtos.SessionResponse;

public class BasicReplier extends SolaceBase {
    private static final Logger log = LoggerFactory.getLogger(BasicRequestor.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    public void run() throws Exception {
        log.info("BasicReplier is connecting to Solace messaging at {}", HOST);

        SessionResponse sessionResponse = ReqRepHelper.createSession();
        log.info("Connected to the Solace Message VPN {} with client username: {}", VPN_NAME, USERNAME);

        Topic requestTopic = sessionResponse.Session.createTopic(TOPIC_NAME);
        MessageConsumer requestConsumer = sessionResponse.Session.createConsumer(requestTopic);
        final MessageProducer replyProducer = sessionResponse.Session.createProducer(null);

        requestConsumer.setMessageListener(request -> {
            try {
                Destination replyDestination = request.getJMSReplyTo();
                log.info("Destination: {}", replyDestination.toString());

                if (request instanceof TextMessage) {
                    log.info("Request received:\n{}", ((TextMessage) request).getText());
                }

                log.info("Received request, responding...");

                TextMessage reply = sessionResponse.Session.createTextMessage();
                reply.setBooleanProperty(SupportedProperty.SOLACE_JMS_PROP_IS_REPLY_MESSAGE, Boolean.TRUE);
                reply.setJMSCorrelationID(request.getJMSCorrelationID());
                reply.setText(RESPONSE);

                replyProducer.send(replyDestination, reply, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY,
                        Message.DEFAULT_TIME_TO_LIVE);
                log.info("Responded successfully. Exiting...");

                latch.countDown();
            } catch (JMSException ex) {
                log.error("Error processing incoming message.", ex);
            }
        });

        sessionResponse.Connection.start();
        log.info("Awaiting request...");
        latch.await();

        sessionResponse.Connection.stop();
        replyProducer.close();
        requestConsumer.close();
        sessionResponse.Session.close();
        sessionResponse.Connection.close();
    }
}
