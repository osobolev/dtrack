package btrack.actions;

import btrack.dao.BugsDao;
import btrack.dao.ConnectionProducer;
import btrack.dao.PriorityBean;
import btrack.dao.ValidationException;
import freemarker.template.TemplateException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NewBugServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(NewBugServlet.class);

    private static final PolicyFactory IMAGES = new HtmlPolicyBuilder()
        .allowElements("img")
        .allowUrlProtocols("data")
        .allowAttributes("alt", "src", "data-filename", "border", "height", "width").onElements("img")
        .toFactory();

    private static final PolicyFactory POLICY = IMAGES
        .and(Sanitizers.FORMATTING)
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.STYLES)
        .and(Sanitizers.LINKS)
        .and(Sanitizers.TABLES);

    private final ConnectionProducer dataSource;

    public NewBugServlet(ConnectionProducer dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int projectId = 1; // todo: get from where???
            int userId = 1; // todo: get from where???
            List<PriorityBean> priorities = new BugsDao(dataSource).listPriorities(projectId, userId);
            Map<String, Object> params = new HashMap<>();
            String projectName = "ЫЫЫ"; // todo: get by ID
            params.put("project", projectName);
            params.put("priorities", priorities);
            TemplateUtil.process("newbug.ftl", params, resp);
        } catch (SQLException | ValidationException | TemplateException ex) {
            logger.error("Error", ex);
        }
    }

    private static final class BugCreator {

        final int projectId;
        final int userId;
        private Integer bugId = null;

        String title = null;
        String untrustedHtml = null;
        String priority = null;

        BugCreator(int projectId, int userId) {
            this.projectId = projectId;
            this.userId = userId;
        }

        int getBugId(ConnectionProducer dataSource) throws ValidationException, SQLException {
            if (bugId == null) {
                if (title == null || untrustedHtml == null || priority == null)
                    throw new ValidationException("Заданы не все параметры");
                String safeHtml = POLICY.sanitize(untrustedHtml);
                bugId = new BugsDao(dataSource).newBug(projectId, userId, Integer.parseInt(priority), title, safeHtml);
            }
            return bugId.intValue();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
            fileItemFactory.setDefaultCharset("UTF-8");
            ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
            List<FileItem> fileItems = fileUpload.parseRequest(req);
            int projectId = 1; // todo: get from where???
            int userId = 1; // todo: get from where???
            BugCreator creator = new BugCreator(projectId, userId);
            for (FileItem fileItem : fileItems) {
                if (!fileItem.isFormField()) {
                    if (fileItem.getName() == null || fileItem.getName().isEmpty())
                        continue;
                    int bugId = creator.getBugId(dataSource);
                    String fileName = new File(fileItem.getName()).getName();
                    try (InputStream is = fileItem.getInputStream()) {
                        new BugsDao(dataSource).addBugAttachment(bugId, userId, fileName, is);
                    }
                } else {
                    String fieldName = fileItem.getFieldName();
                    if (fieldName == null)
                        continue;
                    String value = fileItem.getString();
                    switch (fieldName) {
                    case "title":
                        creator.title = value;
                        break;
                    case "html":
                        creator.untrustedHtml = value;
                        break;
                    case "priority":
                        creator.priority = value;
                        break;
                    }
                }
            }
            int bugId = creator.getBugId(dataSource);
            resp.sendRedirect("viewbug.html?bugId=" + bugId);
        } catch (FileUploadException | NumberFormatException | SQLException | ValidationException ex) {
            logger.error("Error", ex);
        }
    }
}
