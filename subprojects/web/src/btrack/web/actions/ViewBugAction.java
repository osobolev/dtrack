package btrack.web.actions;

import btrack.web.dao.BugViewDao;
import btrack.web.data.*;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ViewBugAction extends Action {

    private final int bugId;
    private final ProjectInfo request;

    public ViewBugAction(int bugId, ProjectInfo request) {
        this.bugId = bugId;
        this.request = request;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws SQLException, ValidationException, IOException, TemplateException, NoAccessException {
        render(ctx, resp, null);
    }

    void render(Context ctx, HttpServletResponse resp, String error) throws SQLException, NoAccessException, IOException, TemplateException {
        BugViewDao dao = new BugViewDao(ctx.connection);
        BugBean bug = dao.loadBug(bugId, request);
        if (bug == null)
            throw new NoAccessException("Bug " + bugId + " not found", HttpServletResponse.SC_NOT_FOUND);
        List<TransitionBean> transitions = dao.listTransitions(request.projectId, bug.stateCode);
        List<AttachmentBean> attachments = dao.listBugAttachments(bugId);
        List<ChangeBean> allChanges = dao.loadBugHistory(bug.bugNum, bugId, request);
        List<ChangeListBean> changes = new ArrayList<>();
        List<ChangeBean> comments = allChanges.stream().filter(ChangeBean::hasComments).collect(Collectors.toList());
        changes.add(new ChangeListBean("comments", "Комментарии", comments));
        changes.add(new ChangeListBean("changes", "Все изменения", allChanges));
        List<UserBean> users = new ArrayList<>();
        Integer toSkip;
        if (bug.getAssignedUserId() != null && bug.getAssignedUserId().intValue() == request.getUserId()) {
            toSkip = null;
        } else {
            toSkip = request.getUserId();
            users.add(new UserBean(request.getUserId(), "Назначить мне"));
        }
        dao.listPossibleAssignees(request.projectId, toSkip, users);
        Map<String, Object> params = new HashMap<>();
        request.putTo(params);
        params.put("bug", bug);
        params.put("transitions", transitions);
        params.put("attachments", attachments);
        params.put("changes", changes);
        params.put("users", users);
        params.put("error", error);
        TemplateUtil.process("viewbug.ftl", params, resp.getWriter());
    }
}