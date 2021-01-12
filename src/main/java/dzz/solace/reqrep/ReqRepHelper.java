package dzz.solace.reqrep;

import javax.jms.Connection;
import javax.jms.Session;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

import dzz.solace.SolaceBase;
import dzz.solace.reqrep.dtos.SessionResponse;

/**
 * Request/Reply helper.
 *
 * Based on <a href=
 * 'https://solace.com/samples/solace-samples-java/request-reply/'>Solace
 * samples.</a>
 */
public class ReqRepHelper extends SolaceBase {

    public static SessionResponse createSession() throws Exception {
        SolConnectionFactory factory = SolJmsUtility.createConnectionFactory();

        factory.setHost(HOST);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setVPN(VPN_NAME);
        factory.setSSLTrustStore(SSL_TRUSTSTORE);
        factory.setSSLTrustStorePassword(SSL_TRUSTSTORE_PASSWORD);
        factory.setSSLValidateCertificate(SSL_VALIDATE_CERTIFICATE);

        Connection connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        return new SessionResponse(connection, session);
    }
}