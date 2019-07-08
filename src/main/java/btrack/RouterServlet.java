package btrack;

import btrack.actions.*;
import btrack.dao.BugViewDao;
import btrack.dao.ProjectBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class RouterServlet extends BaseServlet {

    public RouterServlet(ConnectionProducer dataSource) {
        super(dataSource);
    }

    private static final class PathInfo {

        static final PathInfo ROOT = new PathInfo(null, null, null, null);

        final String projectName;
        final ProjectItem item;
        final Integer num;
        final String page;

        PathInfo(String projectName, ProjectItem item, Integer num, String page) {
            this.projectName = projectName;
            this.item = item;
            this.num = num;
            this.page = page;
        }
    }

    private static PathInfo parse(HttpServletRequest req) throws ValidationException {
        String path = req.getPathInfo();
        if (path == null || path.isEmpty() || "/".equals(path))
            return PathInfo.ROOT;
        String[] parts = path.substring(1).split("/");
        if (parts.length <= 0)
            return PathInfo.ROOT;
        String projectName = parts[0];
        if (parts.length > 1) {
            String part1 = parts[1];
            for (ProjectItem item : ProjectItem.values()) {
                if (item.name().equalsIgnoreCase(part1)) {
                    if (parts.length > 2) {
                        String strNum = parts[2];
                        int num = Context.parseInt(strNum);
                        String page;
                        if (parts.length > 3) {
                            page = parts[3];
                        } else {
                            page = null;
                        }
                        return new PathInfo(projectName, item, num, page);
                    } else {
                        return new PathInfo(projectName, null, null, null);
                    }
                }
            }
            return new PathInfo(projectName, null, null, part1);
        } else {
            return new PathInfo(projectName, null, null, null);
        }
    }

    protected Action getAction(Connection connection, HttpServletRequest req, UserInfo user) throws NoAccessException, SQLException, ValidationException {
        String webRoot = Context.getWebRoot(req);
        if (user == null) {
            StringBuilder url = new StringBuilder(req.getRequestURI());
            String queryString = req.getQueryString();
            if (queryString != null) {
                url.append('?').append(queryString);
            }
            return new LoginAction(webRoot, url.toString());
        }
        PathInfo info = parse(req);
        BugViewDao dao = new BugViewDao(connection);
        String projectRoot = webRoot + req.getServletPath();
        List<ProjectBean> availableProjects = dao.listAvailableProjects(user.id, projectRoot);
        if (info.projectName != null) {
            String projectName = info.projectName;
            Integer maybeProjectId = dao.getProjectId(projectName);
            if (maybeProjectId == null) {
                throw new NoAccessException("Project not found: " + projectName, HttpServletResponse.SC_NOT_FOUND);
            }
            int projectId = maybeProjectId.intValue();
            if (!dao.userHasAccess(projectId, user.id)) {
                throw new NoAccessException("User " + user.id + " has no access to project " + projectName, HttpServletResponse.SC_FORBIDDEN);
            }
            String projectBase = ProjectBean.getProjectBase(projectRoot, projectName);
            CommonInfo common = new CommonInfo(projectId, projectName, webRoot, projectBase, user, availableProjects);
            String page = info.page;
            if (info.item != null && info.num != null) {
                int num = info.num.intValue();
                switch (info.item) {
                case BUG:
                    Integer maybeBugId = dao.getBugId(projectId, num);
                    if (maybeBugId == null) {
                        throw new NoAccessException("Bug not found: " + projectName + "/" + num, HttpServletResponse.SC_NOT_FOUND);
                    }
                    int bugId = maybeBugId.intValue();
                    if ("edit.html".equals(page)) {
                        return new EditBugAction(bugId, common);
                    } else if ("comment.html".equals(page)) {
                        return new AddCommentAction(bugId, num, common);
                    } else if ("assign.html".equals(page)) {
                        return new AssignAction(bugId, num, common);
                    } else if ("move.html".equals(page)) {
                        return new MoveStateAction(bugId, num, common);
                    }
                    // todo: удаление комментов
                    return new ViewBugAction(bugId, common);
                case FILE:
                    return new AttachmentAction(false, num);
                case CFILE:
                    return new AttachmentAction(true, num);
                case REPORT:
                    return new ViewReportAction(common);
                }
            } else {
                if ("newbug.html".equals(page)) {
                    return new NewBugAction(common);
                }
            }
            return new ReportListAction(common);
        } else {
            return new ProjectListAction(webRoot, availableProjects, user);
        }
    }
}
