package btrack;

import btrack.actions.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;

public final class LoginServlet extends BaseServlet {

    public LoginServlet(ConnectionProducer dataSource) {
        super(dataSource);
    }

    protected Action getAction(Connection connection, HttpServletRequest req, UserInfo user) throws NoAccessException, SQLException, ValidationException {
        String webRoot = Context.getWebRoot(req);
        return new LoginAction(webRoot, null);
    }
}
