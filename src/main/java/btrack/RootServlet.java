package btrack;

import btrack.actions.RequestInfo;
import btrack.dao.BugViewDao;
import btrack.dao.ProjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public final class RootServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConnectionProducer dataSource;

    public RootServlet(ConnectionProducer dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserInfo user = (UserInfo) req.getSession().getAttribute(UserInfo.ATTRIBUTE);
        String webRoot = RequestInfo.getWebRoot(req);
        if (user == null) {
            resp.sendRedirect(webRoot + "/login.html");
        } else {
            try (Connection connection = dataSource.getConnection()) {
                String projectRoot = webRoot + "/p";
                List<ProjectBean> projects = new BugViewDao(connection).listAvailableProjects(user.id, projectRoot);
                if (projects.size() == 1) {
                    resp.sendRedirect(projects.get(0).getViewLink());
                } else {
                    resp.sendRedirect(projectRoot);
                }
            } catch (Exception ex) {
                logger.error("Error", ex);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
