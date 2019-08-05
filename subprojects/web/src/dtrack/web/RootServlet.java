package dtrack.web;

import dtrack.common.ConnectionProducer;
import dtrack.web.actions.RequestInfo;
import dtrack.web.dao.BugViewDao;
import dtrack.web.data.ProjectBean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

final class RootServlet extends HttpServlet {

    private final Logger logger;
    private final ConnectionProducer dataSource;

    RootServlet(Logger logger, ConnectionProducer dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserInfo user = UserInfo.get(req, logger);
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
                logger.error(ex);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
