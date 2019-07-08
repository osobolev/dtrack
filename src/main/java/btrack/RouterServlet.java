package btrack;

import btrack.actions.*;
import btrack.dao.BugViewDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;

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
                        int num = AccessUtil.parseInt(strNum);
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
        PathInfo info = parse(req);
        if (info.projectName != null) {
            if (user == null) {
                StringBuffer url = req.getRequestURL();
                String queryString = req.getQueryString();
                if (queryString != null) {
                    url.append('?').append(queryString);
                }
                return new LoginAction(url.toString());
            }
            String projectName = info.projectName;
            BugViewDao dao = new BugViewDao(connection);
            Integer maybeProjectId = dao.getProjectId(projectName);
            if (maybeProjectId == null) {
                throw new NoAccessException("Project not found: " + projectName, HttpServletResponse.SC_NOT_FOUND);
            }
            int projectId = maybeProjectId.intValue();
            if (!dao.userHasAccess(projectId, user.id)) {
                throw new NoAccessException("User " + user.id + " has no access to project " + projectName, HttpServletResponse.SC_FORBIDDEN);
            }
            String projectBase = req.getServletPath() + "/" + projectName;
            CommonInfo common = new CommonInfo(projectId, projectName, projectBase, user);
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
                        return new EditBugAction(bugId, num, common);
                    } else if ("comment.html".equals(page)) {
                        return new AddCommentAction(bugId, num, common);
                    } else if ("assign.html".equals(page)) {
                        return new AssignAction(bugId, num, common);
                    } else if ("move.html".equals(page)) {
                        return new MoveStateAction(bugId, num, common);
                    }
                    // todo: удаление комментов
                    return new ViewBugAction(bugId, num, common);
                case FILE:
                    return new AttachmentAction(false, num);
                case CFILE:
                    return new AttachmentAction(true, num);
                case REPORT:
                    return null; // todo: view report action
                }
            } else {
                if ("newbug.html".equals(page)) {
                    return new NewBugAction(common);
                }
            }
            return null; // todo: view project reports action
        } else {
            return null;
        }
    }
}
