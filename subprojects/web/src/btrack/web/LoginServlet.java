package btrack.web;

import btrack.common.ConnectionProducer;
import btrack.web.actions.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;

final class LoginServlet extends BaseServlet {

    private final boolean debug;

    LoginServlet(Logger logger, ConnectionProducer dataSource, boolean debug) {
        super(logger, dataSource);
        this.debug = debug;
    }

    protected Action getAction(Connection connection, HttpServletRequest req, UserInfo user) throws NoAccessException, SQLException, ValidationException {
        RequestInfo request = new RequestInfo(RequestInfo.getWebRoot(req), RequestInfo.getClientLocale(req));
        return new LoginAction(debug, null, request);
    }
}
