package dtrack.web.actions;

import dtrack.web.dao.BugEditDao;
import smalljson.JSONObject;
import smalljson.JSONWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class MoveStateAction extends Action {

    private final int bugId;
    private final ProjectInfo request;

    public MoveStateAction(int bugId, ProjectInfo request) {
        this.bugId = bugId;
        this.request = request;
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        BugEditDao dao = new BugEditDao(ctx.connection);
        if (!dao.validateTransition(request.projectId, from, to)) {
            throw new ValidationException("Cannot move from " + from + " to " + to);
        }
        Integer[] changeBox = new Integer[1];
        boolean ok = dao.changeBugState(bugId, request.getUserId(), changeBox, from, to);
        JSONObject object = new JSONObject();
        if (ok) {
            ctx.connection.commit();
            object.put("status", "ok");
        } else {
            object.put("status", "error");
            String error = "Другой пользователь уже изменил состояние";
            object.put("message", error);
        }
        JSONWriter.writeTo(object, resp.getWriter());
    }
}
