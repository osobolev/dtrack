package btrack.actions;

import btrack.dao.BugEditDao;
import btrack.dao.BugViewDao;
import btrack.data.PriorityBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NewBugAction extends Action {

    private final ProjectInfo request;

    public NewBugAction(ProjectInfo request) {
        this.request = request;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        BugViewDao dao = new BugViewDao(ctx.connection);
        List<PriorityBean> priorities = dao.listPriorities(request.projectId, null);
        Map<String, Object> params = new HashMap<>();
        request.putTo(params);
        params.put("postLink", request.getNewBugUrl());
        params.put("priorities", priorities);
        TemplateUtil.process("newbug.ftl", params, resp.getWriter());
    }

    private static final class BugCoords {

        final int num;
        final int id;

        BugCoords(int num, int id) {
            this.num = num;
            this.id = id;
        }
    }

    private BugCoords createBug(BugEditDao dao, Map<String, String> parameters) throws ValidationException, SQLException {
        BugData data = BugData.create(dao, request, parameters);
        String stateCode = dao.getDefaultState(request.projectId, request.getUserId());
        if (stateCode == null) {
            throw new ValidationException("No default state for project " + request.projectName);
        }
        int num = dao.getNextBugId(request.projectId);
        int id = dao.newBug(request.projectId, request.getUserId(), num, data.priorityCode, stateCode, data.title, data.safeHtml);
        return new BugCoords(num, id);
    }

    @Override
    public String post(Context ctx, HttpServletRequest req) throws Exception {
        BugEditDao dao = new BugEditDao(ctx.connection);
        UploadUtil<BugCoords> util = new UploadUtil<>(parameters -> createBug(dao, parameters));
        BugCoords bugCoords = util.post(
            req,
            (bug, fileName, content) -> dao.addBugAttachment(bug.id, fileName, content)
        );
        ctx.connection.commit();
        return request.getBugUrl(bugCoords.num);
    }
}
