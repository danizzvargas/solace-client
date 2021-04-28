package dzz.solace.jcsmp;

import dzz.solace.SolaceBase;
import com.solacesystems.jcsmp.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class SolaceClientJCSMP extends SolaceBase {
    private static final Logger log = LoggerFactory.getLogger(SolaceBase.class);

    public static void main(String[] args) throws JCSMPException {
        log.info("====== JCSMP client ======");

        log.info("Connecting");
        final JCSMPSession session = JcsmpHelper.createSession();
        session.connect();

        System.out.println("\nSelect one:");
        System.out.println("  'c'. Consumer");
        System.out.println("  'p'. Producer");
        System.out.print("> ");

        String option = args.length > 1 ? args[1] : new Scanner(System.in).next();
        log.info("Input: {}", option);

        switch (option) {
            case "c":
                log.info("Consumer selected");
                receiveMessage(session);
                break;
            case "p":
                log.info("Producer selected");
                sendMessage(session);
                break;
            default:
                log.info("No valid option selected. Use 'c' for consumer or 'p' for producer.");
        }

        log.info("Done");
    }

    private static void receiveMessage(JCSMPSession session) throws JCSMPException {
        final CountDownLatch latch = new CountDownLatch(1);
        final XMLMessageConsumer consumer = JcsmpHelper.createConsumer(session, latch);
        final Topic topic = JCSMPFactory.onlyInstance().createTopic(TOPIC_NAME);

        session.addSubscription(topic);
        consumer.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("I was awoken while waiting", e);
        }
    }

    private static void sendMessage(JCSMPSession session) throws JCSMPException {
        final XMLMessageProducer prod = JcsmpHelper.createProducer(session);
        final Topic topic = JCSMPFactory.onlyInstance().createTopic(TOPIC_NAME);

        // XML message.
        XMLContentMessage xmlMsg = JCSMPFactory.onlyInstance().createMessage(XMLContentMessage.class);
        SDTMap map = JCSMPFactory.onlyInstance().createMap();
        map.putString("event_type", "my-event");
        map.putString("event_version", "1.0.0");
        xmlMsg.setProperties(map);

        try (Scanner keyboard = new Scanner(System.in)) {
            System.out.print("> ");
            String msg = keyboard.next();
            xmlMsg.setXMLContent(msg);
            prod.send(xmlMsg, topic);
        }

        // TextMessage msg =
        // JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        // final String text = "This is a dummy message!";
        // msg.setText(text);
        // prod.send(msg, topic);
    }
}
