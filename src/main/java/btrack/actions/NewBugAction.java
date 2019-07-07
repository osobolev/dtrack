package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.BugEditDao;
import btrack.dao.BugViewDao;
import btrack.dao.PriorityBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NewBugAction extends Action {

    private final int projectId;
    private final String projectName;
    private final int userId;

    public NewBugAction(int projectId, String projectName, int userId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.userId = userId;
    }

    @Override
    public void get(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        BugViewDao dao = new BugViewDao(ctx.connection);
        List<PriorityBean> priorities = dao.listPriorities(projectId, null);
        Map<String, Object> params = new HashMap<>();
        params.put("project", projectName);
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
        BugData data = BugData.create(dao, projectId, parameters);
        Integer stateId = dao.getDefaultState(projectId);
        if (stateId == null) {
            throw new ValidationException("No default state for project " + projectId);
        }
        int num = dao.getNextBugId(projectId);
        int id = dao.newBug(projectId, userId, num, data.priorityId, stateId.intValue(), data.title, data.safeHtml);
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
        return AccessUtil.getBugUrl(req, projectName, bugCoords.num);
    }
}
