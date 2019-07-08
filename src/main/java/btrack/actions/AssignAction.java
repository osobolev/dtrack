package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.BugEditDao;
import btrack.dao.BugViewDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class AssignAction extends Action {

    private final int bugId;
    private final int bugNum;
    private final CommonInfo common;

    public AssignAction(int bugId, int bugNum, CommonInfo common) {
        this.bugId = bugId;
        this.bugNum = bugNum;
        this.common = common;
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
            if (!vdao.userHasAccess(common.projectId, newUserId.intValue())) {
                throw new NoAccessException("User " + newUserId + " has no access to project " + common.projectName, HttpServletResponse.SC_FORBIDDEN);
            }
        }
        Integer[] changeBox = new Integer[1];
        BugEditDao dao = new BugEditDao(ctx.connection);
        boolean ok = dao.changeAssignedUser(bugId, common.getUserId(), changeBox, oldUserId, newUserId);
        if (ok) {
            ctx.connection.commit();
            resp.sendRedirect(common.getBugUrl(bugNum));
        } else {
            String error = "Другой пользователь уже изменил исполнителя";
            new ViewBugAction(bugId, common).render(ctx, resp, error);
        }
    }
}
