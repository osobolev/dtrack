package dtrack.web.actions;

import dtrack.web.dao.BugViewDao;
import dtrack.web.data.*;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
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
        Set<Integer> toSkip = new HashSet<>();
        if (bug.assignedUserId != null) {
            toSkip.add(bug.assignedUserId);
        }
        List<UserBean> users = new ArrayList<>();
        if (!Objects.equals(bug.assignedUserId, request.getUserId())) {
            toSkip.add(request.getUserId());
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
        TemplateUtil.process("viewbug.ftl", params, resp.getWriter());
    }
}
