package dzz.solace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SolaceBase {
    private static final Logger log = LoggerFactory.getLogger(SolaceBase.class);

    private static final Properties properties = new Properties();

    protected static final String HOST;
    protected static final String USERNAME;
    protected static final String VPN_NAME;
    protected static final String PASSWORD;
    protected static final String TOPIC_NAME;
    protected static final String RESPONSE;
    protected static final String SSL_TRUSTSTORE;
    protected static final String SSL_TRUSTSTORE_PASSWORD;
    protected static final boolean SSL_VALIDATE_CERTIFICATE;

    static {
        try (InputStream resource = SolaceBase.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(resource);

            for (Object key : properties.keySet()) {
                String override = System.getProperty((String) key);
                if (override != null) {
                    properties.put(key, override);
                }
            }

        } catch (IOException ex) {
            log.error("Error trying to retrieve properties.", ex);
        }

        HOST = properties.getProperty("solace.host");
        USERNAME = properties.getProperty("solace.username");
        VPN_NAME = properties.getProperty("solace.vpn_name");
        PASSWORD = properties.getProperty("solace.password");
        TOPIC_NAME = properties.getProperty("solace.topic_name");
        RESPONSE = properties.getProperty("solace.response");
        SSL_TRUSTSTORE = properties.getProperty("solace.ssl.truststore");
        SSL_TRUSTSTORE_PASSWORD = properties.getProperty("solace.ssl.truststore.password");
        SSL_VALIDATE_CERTIFICATE = Boolean.parseBoolean(properties.getProperty("solace.ssl.validate_certificate"));

        showConfiguration();
    }

    private static void showConfiguration() {
        log.info("Solace host: {}", HOST);
        log.info("Solace username: {}", USERNAME);
        log.info("Solace VPN name: {}", VPN_NAME);
        log.info("Solace topic name: {}", TOPIC_NAME);
        log.info("Response: {}", RESPONSE);
        log.info("SSL truststore: {}", SSL_TRUSTSTORE);
        log.info("SSL validate certificate: {}", SSL_VALIDATE_CERTIFICATE);
    }
}
