package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.BugEditDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class MoveStateAction extends Action {

    private final int bugId;
    private final int bugNum;
    private final CommonInfo common;

    public MoveStateAction(int bugId, int bugNum, CommonInfo common) {
        this.bugId = bugId;
        this.bugNum = bugNum;
        this.common = common;
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        int fromId = AccessUtil.parseInt(from);
        int toId = AccessUtil.parseInt(to);
        BugEditDao dao = new BugEditDao(ctx.connection);
        if (!dao.validateTransition(common.projectId, fromId, toId)) {
            throw new ValidationException("Cannot move from " + fromId + " to " + toId);
        }
        Integer[] changeBox = new Integer[1];
        boolean ok = dao.changeBugState(bugId, common.getUserId(), changeBox, fromId, toId);
        if (ok) {
            ctx.connection.commit();
            resp.sendRedirect(common.getBugUrl(bugNum));
        } else {
            String error = "Другой пользователь уже изменил состояние";
            new ViewBugAction(bugId, common).render(ctx, resp, error);
        }
    }
}
