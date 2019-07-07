package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.BugEditDao;
import btrack.dao.BugViewDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class AssignAction extends Action {

    private final int projectId;
    private final String projectName;
    private final int bugId;
    private final int bugNum;
    private final int userId;

    public AssignAction(int projectId, String projectName, int bugId, int bugNum, int userId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.bugId = bugId;
        this.bugNum = bugNum;
        this.userId = userId;
    }

    private static Integer parseUserId(String str) throws ValidationException {
        if (str == null || str.trim().isEmpty()) {
            return null;
        } else {
            return AccessUtil.parseInt(str);
        }
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer oldUserId = parseUserId(req.getParameter("oldUserId"));
        Integer newUserId = parseUserId(req.getParameter("newUserId"));
        if (newUserId != null) {
            BugViewDao vdao = new BugViewDao(ctx.connection);
            if (!vdao.userHasAccess(projectId, newUserId.intValue())) {
                throw new NoAccessException("User " + newUserId + " has no access to project " + projectName, HttpServletResponse.SC_FORBIDDEN);
            }
        }
        Integer[] changeBox = new Integer[1];
        BugEditDao dao = new BugEditDao(ctx.connection);
        boolean ok = dao.changeAssignedUser(bugId, userId, changeBox, oldUserId, newUserId);
        if (ok) {
            ctx.connection.commit();
            resp.sendRedirect(AccessUtil.getBugUrl(req, projectName, bugNum));
        } else {
            String error = "Другой пользователь уже изменил исполнителя";
            new ViewBugAction(projectId, projectName, bugId, bugNum, userId).render(ctx, req, resp, error);
        }
    }
}
