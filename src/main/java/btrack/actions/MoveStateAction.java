package btrack.actions;

import btrack.dao.BugEditDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class MoveStateAction extends Action {

    private final int bugId;
    private final int bugNum;
    private final ProjectInfo request;

    public MoveStateAction(int bugId, int bugNum, ProjectInfo request) {
        this.bugId = bugId;
        this.bugNum = bugNum;
        this.request = request;
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        int fromId = Context.parseInt(from);
        int toId = Context.parseInt(to);
        BugEditDao dao = new BugEditDao(ctx.connection);
        if (!dao.validateTransition(request.projectId, fromId, toId)) {
            throw new ValidationException("Cannot move from " + fromId + " to " + toId);
        }
        Integer[] changeBox = new Integer[1];
        boolean ok = dao.changeBugState(bugId, request.getUserId(), changeBox, fromId, toId);
        if (ok) {
            ctx.connection.commit();
            resp.sendRedirect(request.getBugUrl(bugNum));
        } else {
            String error = "Другой пользователь уже изменил состояние";
            new ViewBugAction(bugId, request).render(ctx, resp, error);
        }
    }
}
