package btrack;

import btrack.actions.Action;
import btrack.actions.Context;
import btrack.actions.NoAccessException;
import btrack.actions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

abstract class BaseServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConnectionProducer dataSource;

    protected BaseServlet(ConnectionProducer dataSource) {
        this.dataSource = dataSource;
    }

    protected abstract Action getAction(Connection connection, HttpServletRequest req, UserInfo user) throws Exception;

    private void perform(boolean get, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        try (Connection connection = dataSource.getConnection()) {
            UserInfo user = (UserInfo) req.getSession().getAttribute(UserInfo.ATTRIBUTE);
            Action action = getAction(connection, req, user);
            if (action == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                Context ctx = new Context(connection);
                if (get) {
                    action.get(ctx, resp);
                } else {
                    action.post(ctx, req, resp);
                }
            }
        } catch (NoAccessException ex) {
            logger.error(ex.getMessage(), ex);
            resp.sendError(ex.code);
        } catch (ValidationException ex) {
            logger.error(ex.getMessage(), ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception ex) {
            logger.error("Error", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        perform(true, req, resp);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        perform(false, req, resp);
    }
}
