package dtrack.web.actions;

import dtrack.web.dao.BugEditDao;
import dtrack.web.dao.BugViewDao;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class AssignAction extends Action {

    private final int bugId;
    private final ProjectInfo request;

    public AssignAction(int bugId, ProjectInfo request) {
        this.bugId = bugId;
        this.request = request;
    }

    private static Integer parseUserId(String str) throws ValidationException {
        if (str == null || str.trim().isEmpty()) {
            return null;
        } else {
            return Context.parseInt(str);
        }
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer oldUserId = parseUserId(req.getParameter("oldUserId"));
        Integer newUserId = parseUserId(req.getParameter("newUserId"));
        if (newUserId != null) {
            BugViewDao vdao = new BugViewDao(ctx.connection);
            if (!vdao.userHasAccess(request.projectId, newUserId.intValue())) {
                throw new NoAccessException("User " + newUserId + " has no access to project " + request.projectName, HttpServletResponse.SC_FORBIDDEN);
            }
        }
        Integer[] changeBox = new Integer[1];
        BugEditDao dao = new BugEditDao(ctx.connection);
        boolean ok = dao.changeAssignedUser(bugId, request.getUserId(), changeBox, oldUserId, newUserId);
        JSONObject object = new JSONObject();
        if (ok) {
            ctx.connection.commit();
            object.put("status", "ok");
        } else {
            object.put("status", "error");
            String error = "Другой пользователь уже изменил исполнителя";
            object.put("message", error);
        }
        object.write(resp.getWriter());
    }
}
