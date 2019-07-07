package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.*;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EditBugAction extends Action {

    private static final String FILE_KEY_START = "file_";

    private final int projectId;
    private final String projectName;
    private final int bugId;
    private final int bugNum;
    private final int userId;

    public EditBugAction(int projectId, String projectName, int bugId, int bugNum, int userId) {
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
        List<AttachmentBean> attachments = dao.listBugAttachments(bugId);
        List<PriorityBean> priorities = dao.listPriorities(projectId, bug.getPriorityId());
        Map<String, Object> params = new HashMap<>();
        params.put("bug", bug);
        params.put("attachments", attachments);
        params.put("priorities", priorities);
        TemplateUtil.process("editbug.ftl", params, resp.getWriter());
    }

    @Override
    public String post(Context ctx, HttpServletRequest req) throws Exception {
        BugViewDao vdao = new BugViewDao(ctx.connection);
        String projectBase = AccessUtil.getProjectBase(req, projectName);
        BugBean bug = vdao.loadBug(projectName, bugId, bugNum, projectBase);
        if (bug == null)
            throw new NoAccessException("Bug " + bugId + " not found", HttpServletResponse.SC_NOT_FOUND);
        BugEditDao dao = new BugEditDao(ctx.connection);
        Integer[] changeBox = new Integer[1];
        UploadUtil<Void> util = new UploadUtil<>(parameters -> {
            BugData data = BugData.create(dao, projectId, parameters);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(FILE_KEY_START)) {
                    String value = entry.getValue();
                    if (!Boolean.parseBoolean(value)) {
                        int attachmentId = AccessUtil.parseInt(key.substring(FILE_KEY_START.length()));
                        int changeId = dao.getChangeId(bugId, userId, changeBox);
                        dao.removeBugAttachment(changeId, attachmentId, userId);
                    }
                }
            }
            dao.changeBug(bugId, userId, changeBox, data.priorityId, data.title, data.safeHtml);
            return null;
        });
        util.post(req, (result, fileName, content) -> {
            int attachmentId = dao.addBugAttachment(bugId, fileName, content);
            int changeId = dao.getChangeId(bugId, userId, changeBox);
            dao.addBugAttachmentChange(changeId, attachmentId);
        });
        ctx.connection.commit();
        return bug.getViewLink();
    }
}