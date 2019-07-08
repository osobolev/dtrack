package btrack;

import btrack.actions.Action;
import btrack.actions.LoginAction;
import btrack.actions.NoAccessException;
import btrack.actions.ValidationException;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;

public final class LoginServlet extends BaseServlet {

    public LoginServlet(ConnectionProducer dataSource) {
        super(dataSource);
    }

    protected Action getAction(Connection connection, HttpServletRequest req, UserInfo user) throws NoAccessException, SQLException, ValidationException {
        return new LoginAction(null);
    }
}
