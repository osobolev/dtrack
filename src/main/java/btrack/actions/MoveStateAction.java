package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.BugEditDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class MoveStateAction extends Action {

    private final int projectId;
    private final String projectName;
    private final int bugId;
    private final int bugNum;
    private final int userId;

    public MoveStateAction(int projectId, String projectName, int bugId, int bugNum, int userId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.bugId = bugId;
        this.bugNum = bugNum;
        this.userId = userId;
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        int fromId = AccessUtil.parseInt(from);
        int toId = AccessUtil.parseInt(to);
        BugEditDao dao = new BugEditDao(ctx.connection);
        if (!dao.validateTransition(projectId, fromId, toId)) {
            throw new ValidationException("Cannot move from " + fromId + " to " + toId);
        }
        Integer[] changeBox = new Integer[1];
        boolean ok = dao.changeBugState(bugId, userId, changeBox, fromId, toId);
        if (ok) {
            ctx.connection.commit();
            resp.sendRedirect(AccessUtil.getBugUrl(req, projectName, bugNum));
        } else {
            String error = "Другой пользователь уже изменил состояние";
            new ViewBugAction(projectId, projectName, bugId, bugNum, userId).render(ctx, req, resp, error);
        }
    }
}
