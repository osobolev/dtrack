package btrack.actions;

import btrack.dao.BugBean;
import btrack.dao.BugsDao;
import btrack.dao.ConnectionProducer;
import btrack.dao.ValidationException;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class ViewBugServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(ViewBugServlet.class);

    private final ConnectionProducer dataSource;

    public ViewBugServlet(ConnectionProducer dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int bugId = Integer.parseInt(req.getParameter("bugId"));
            int userId = 1; // todo: get from where???
            BugBean bug = new BugsDao(dataSource).loadBug(bugId, userId);
            Map<String, Object> params = new HashMap<>();
            params.put("bug", bug);
            TemplateUtil.process("viewbug.ftl", params, resp);
        } catch (SQLException | ValidationException | TemplateException | NumberFormatException ex) {
            logger.error("Error", ex);
        }
    }
}
