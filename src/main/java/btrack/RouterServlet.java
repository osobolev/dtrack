package btrack;

import btrack.actions.*;
import btrack.dao.BugViewDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public final class RouterServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(RouterServlet.class);

    private final ConnectionProducer dataSource;

    public RouterServlet(ConnectionProducer dataSource) {
        this.dataSource = dataSource;
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

    private static Action getAction(Connection connection, PathInfo info, Integer maybeUserId) throws NoAccessException, SQLException {
        if (info.projectName != null) {
            if (maybeUserId == null) {
                // todo: redirect to login page!!!
                throw new NoAccessException("User not logged in", HttpServletResponse.SC_UNAUTHORIZED);
            }
            int userId = maybeUserId.intValue();
            String projectName = info.projectName;
            BugViewDao dao = new BugViewDao(connection);
            Integer maybeProjectId = dao.getProjectId(projectName);
            if (maybeProjectId == null) {
                throw new NoAccessException("Project not found: " + projectName, HttpServletResponse.SC_NOT_FOUND);
            }
            int projectId = maybeProjectId.intValue();
            if (!dao.userHasAccess(projectId, userId)) {
                throw new NoAccessException("User " + userId + " has no access to project " + projectName, HttpServletResponse.SC_FORBIDDEN);
            }
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
                        return new EditBugAction(projectId, projectName, bugId, num, userId);
                    } else if ("comment.html".equals(page)) {
                        return new AddCommentAction(projectName, bugId, num, userId);
                    } else if ("assign.html".equals(page)) {
                        return new AssignAction(projectId, projectName, bugId, num, userId);
                    } else if ("move.html".equals(page)) {
                        return new MoveStateAction(projectId, projectName, bugId, num, userId);
                    }
                    return new ViewBugAction(projectId, projectName, bugId, num, userId);
                case FILE:
                    return new AttachmentAction(false, num);
                case CFILE:
                    return new AttachmentAction(true, num);
                case REPORT:
                    return null; // todo: view report action
                }
            } else {
                if ("newbug.html".equals(page)) {
                    return new NewBugAction(projectId, projectName, userId);
                }
            }
            return null; // todo: view project reports action
        } else {
            return null;
        }
    }

    private void perform(boolean get, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        try (Connection connection = dataSource.getConnection()) {
            PathInfo info = parse(req);
            Integer userId = (Integer) req.getSession().getAttribute("userId");
            if (userId == null) {
                userId = 1; // todo!!! remove later
            }
            Action action = getAction(connection, info, userId);
            if (action == null) {
                if (get) {
                    // todo: show index page with login link
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                Context ctx = new Context(connection);
                if (get) {
                    action.get(ctx, req, resp);
                } else {
                    action.post(ctx, req, resp);
                }
            }
        } catch (NoAccessException ex) {
            logger.error(ex.getMessage());
            resp.sendError(ex.code);
        } catch (ValidationException ex) {
            logger.error(ex.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception ex) {
            logger.error("Error", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        perform(true, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        perform(false, req, resp);
    }
}
