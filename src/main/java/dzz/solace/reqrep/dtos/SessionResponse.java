package dzz.solace.reqrep.dtos;

import javax.jms.Connection;
import javax.jms.Session;

public class SessionResponse {
    public final Connection Connection;
    public final Session Session;

    public SessionResponse(Connection connection, Session session) {
        this.Connection = connection;
        this.Session = session;
    }
}
