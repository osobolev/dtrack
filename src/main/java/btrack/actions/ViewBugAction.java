package btrack.actions;

import btrack.dao.*;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ViewBugAction extends Action {

    private final int bugId;
    private final CommonInfo common;

    public ViewBugAction(int bugId, CommonInfo common) {
        this.bugId = bugId;
        this.common = common;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws SQLException, ValidationException, IOException, TemplateException, NoAccessException {
        render(ctx, resp, null);
    }

    void render(Context ctx, HttpServletResponse resp, String error) throws SQLException, NoAccessException, IOException, TemplateException {
        BugViewDao dao = new BugViewDao(ctx.connection);
        BugBean bug = dao.loadBug(bugId, common);
        if (bug == null)
            throw new NoAccessException("Bug " + bugId + " not found", HttpServletResponse.SC_NOT_FOUND);
        List<TransitionBean> transitions = dao.listTransitions(common.projectId, bug.getStateId());
        List<AttachmentBean> attachments = dao.listBugAttachments(bugId);
        List<ChangeBean> changes = dao.loadBugHistory(bugId);
        List<UserBean> users = new ArrayList<>();
        Integer toSkip;
        if (bug.getAssignedUserId() != null && bug.getAssignedUserId().intValue() == common.getUserId()) {
            toSkip = null;
        } else {
            toSkip = common.getUserId();
            users.add(new UserBean(common.getUserId(), "Назначить мне"));
        }
        dao.listPossibleAssignees(common.projectId, toSkip, users);
        Map<String, Object> params = new HashMap<>();
        common.putAll(params);
        params.put("bug", bug);
        params.put("transitions", transitions);
        params.put("attachments", attachments);
        params.put("changes", changes);
        params.put("users", users);
        params.put("error", error);
        TemplateUtil.process("viewbug.ftl", params, resp.getWriter());
    }
}
