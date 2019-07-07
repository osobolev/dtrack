package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.BugsDao;
import btrack.dao.PriorityBean;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NewBugAction extends Action {

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

    private final int projectId;
    private final String projectName;
    private final int userId;

    public NewBugAction(int projectId, String projectName, int userId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.userId = userId;
    }

    @Override
    public void get(Context ctx, HttpServletRequest req, Writer out) throws Exception {
        BugsDao dao = new BugsDao(ctx.connection);
        List<PriorityBean> priorities = dao.listPriorities(projectId);
        Map<String, Object> params = new HashMap<>();
        params.put("project", projectName);
        params.put("priorities", priorities);
        TemplateUtil.process("newbug.ftl", params, out);
    }

    private static final class BugCoords {

        final int num;
        final int id;

        BugCoords(int num, int id) {
            this.num = num;
            this.id = id;
        }
    }

    private final class LazyBug {

        final BugsDao dao;
        final int userId;

        private BugCoords bug = null;

        String title = null;
        String untrustedHtml = null;
        String priority = null;

        LazyBug(BugsDao dao, int userId) {
            this.dao = dao;
            this.userId = userId;
        }

        BugCoords getBug() throws ValidationException, SQLException {
            if (bug == null) {
                if (title == null)
                    throw new ValidationException("Missing 'title' parameter");
                if (untrustedHtml == null)
                    throw new ValidationException("Missing 'html' parameter");
                if (priority == null)
                    throw new ValidationException("Missing 'priority' parameter");
                String safeHtml = POLICY.sanitize(untrustedHtml);
                System.out.println(untrustedHtml);
                System.out.println(safeHtml);
                Integer stateId = dao.getDefaultState(projectId);
                if (stateId == null) {
                    throw new ValidationException("No default state for project " + projectId);
                }
                int priorityId = AccessUtil.parseInt(priority);
                if (!dao.validatePriority(projectId, priorityId)) {
                    throw new ValidationException("Wrong priority " + priorityId + " for project " + projectId);
                }
                int num = dao.getNextBugId(projectId);
                int id = dao.newBug(projectId, userId, num, priorityId, stateId.intValue(), title, safeHtml);
                bug = new BugCoords(num, id);
            }
            return bug;
        }
    }

    @Override
    public String post(Context ctx, HttpServletRequest req) throws Exception {
        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
        fileItemFactory.setDefaultCharset("UTF-8");
        ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
        List<FileItem> fileItems = fileUpload.parseRequest(req);
        BugsDao dao = new BugsDao(ctx.connection);
        LazyBug creator = new LazyBug(dao, userId);
        for (FileItem fileItem : fileItems) {
            if (!fileItem.isFormField()) {
                if (fileItem.getName() == null || fileItem.getName().isEmpty())
                    continue;
                BugCoords bug = creator.getBug();
                String fileName = new File(fileItem.getName()).getName();
                try (InputStream is = fileItem.getInputStream()) {
                    dao.addBugAttachment(bug.id, fileName, is);
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
        BugCoords bug = creator.getBug();
        ctx.connection.commit();
        return AccessUtil.getBugUrl(req, projectName, bug.num, null);
    }
}
