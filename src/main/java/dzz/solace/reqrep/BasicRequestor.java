package dzz.solace.reqrep;

import java.util.UUID;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SupportedProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dzz.solace.SolaceBase;
import dzz.solace.reqrep.dtos.SessionResponse;

public class BasicRequestor extends SolaceBase {
    private static final Logger log = LoggerFactory.getLogger(BasicRequestor.class);

    public void run() throws Exception {
        log.info("BasicRequestor is connecting to Solace messaging at {}", HOST);

        SessionResponse sessionResponse = ReqRepHelper.createSession();
        log.info("Connected to the Solace Message VPN {} with client username: {}", VPN_NAME, USERNAME);

        Topic requestTopic = sessionResponse.Session.createTopic(TOPIC_NAME);
        MessageProducer requestProducer = sessionResponse.Session.createProducer(requestTopic);

        // The response will be received on this temporary queue.
        TemporaryQueue replyToQueue = sessionResponse.Session.createTemporaryQueue();
        MessageConsumer replyConsumer = sessionResponse.Session.createConsumer(replyToQueue);

        sessionResponse.Connection.start();

        // Create a request.
        TextMessage request = sessionResponse.Session.createTextMessage("Sample Request");
        request.setJMSReplyTo(replyToQueue);
        String correlationId = UUID.randomUUID().toString();
        request.setJMSCorrelationID(correlationId);

        log.info("Sending request {} to topic {}", request.getText(), requestTopic.toString());
        requestProducer.send(requestTopic, request, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY,
                Message.DEFAULT_TIME_TO_LIVE);

        log.info("Sent successfully. Waiting for reply...");
        int timeout = 1000;
        Message reply = replyConsumer.receive(timeout);

        if (reply == null) {
            throw new Exception("Failed to receive a reply in " + timeout + " msecs");
        }

        if (reply.getJMSCorrelationID() == null) {
            throw new Exception(
                    "Received a reply message with no correlationID. This field is needed for a direct request.");
        }

        if (!reply.getJMSCorrelationID().replaceAll("ID:", "").equals(correlationId)) {
            throw new Exception("Received invalid correlationID in reply message.");
        }

        if (reply instanceof TextMessage) {
            log.info("TextMessage response received: {}", ((TextMessage) reply).getText());
            if (!reply.getBooleanProperty(SupportedProperty.SOLACE_JMS_PROP_IS_REPLY_MESSAGE)) {
                log.warn("Received a reply message without the isReplyMsg flag set.");
            }
        } else {
            log.info("Message response received.");
        }

        log.info("Message Content:\n\n{}", SolJmsUtility.dumpMessage(reply));

        sessionResponse.Connection.stop();
        replyConsumer.close();
        requestProducer.close();
        sessionResponse.Session.close();
        sessionResponse.Connection.close();
    }
}