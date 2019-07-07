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
    private final int userId;

    public AssignAction(int projectId, String projectName, int bugId, int userId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.bugId = bugId;
        this.userId = userId;
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String assignedUser = req.getParameter("assignedUserId");
        Integer assignedUserId;
        if (assignedUser == null || assignedUser.trim().isEmpty()) {
            assignedUserId = null;
        } else {
            assignedUserId = AccessUtil.parseInt(assignedUser);
        }
        BugEditDao dao = new BugEditDao(ctx.connection);
        if (assignedUserId != null) {
            BugViewDao vdao = new BugViewDao(ctx.connection);
            if (!vdao.userHasAccess(projectId, assignedUserId.intValue())) {
                throw new NoAccessException("User " + assignedUserId + " has no access to project " + projectName, HttpServletResponse.SC_FORBIDDEN);
            }
        }
        Integer[] changeBox = new Integer[1];
        dao.changeAssignedUser(bugId, userId, changeBox, assignedUserId);
        ctx.connection.commit();
    }
}
