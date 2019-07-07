package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.*;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ViewBugAction extends Action {

    private final int projectId;
    private final String projectName;
    private final int bugId;
    private final int bugNum;
    private final int userId;

    public ViewBugAction(int projectId, String projectName, int bugId, int bugNum, int userId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.bugId = bugId;
        this.bugNum = bugNum;
        this.userId = userId;
    }

    @Override
    public void get(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws SQLException, ValidationException, IOException, TemplateException, NoAccessException {
        BugViewDao dao = new BugViewDao(ctx.connection);
        String projectBase = AccessUtil.getProjectBase(req, projectName);
        BugBean bug = dao.loadBug(projectName, bugId, bugNum, projectBase);
        if (bug == null)
            throw new NoAccessException("Bug " + bugId + " not found", HttpServletResponse.SC_NOT_FOUND);
        List<TransitionBean> transitions = dao.listTransitions(projectId, bug.getStateId());
        List<AttachmentBean> attachments = dao.listBugAttachments(bugId);
        List<ChangeBean> changes = dao.loadBugHistory(bugId);
        List<UserBean> users = new ArrayList<>();
        Integer toSkip;
        if (bug.getAssignedUserId() != null && bug.getAssignedUserId().intValue() == userId) {
            toSkip = null;
        } else {
            toSkip = userId;
            users.add(new UserBean(userId, "Назначить мне"));
        }
        dao.listPossibleAssignees(projectId, toSkip, users);
        Map<String, Object> params = new HashMap<>();
        params.put("bug", bug);
        params.put("transitions", transitions);
        params.put("attachments", attachments);
        params.put("changes", changes);
        params.put("users", users);
        TemplateUtil.process("viewbug.ftl", params, resp.getWriter());
    }
}
