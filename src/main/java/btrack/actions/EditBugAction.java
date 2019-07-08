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

    private final int bugId;
    private final CommonInfo common;

    public EditBugAction(int bugId, CommonInfo common) {
        this.bugId = bugId;
        this.common = common;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws SQLException, ValidationException, IOException, TemplateException, NoAccessException {
        BugViewDao dao = new BugViewDao(ctx.connection);
        BugBean bug = dao.loadBug(bugId, common);
        if (bug == null)
            throw new NoAccessException("Bug " + bugId + " not found", HttpServletResponse.SC_NOT_FOUND);
        List<AttachmentBean> attachments = dao.listBugAttachments(bugId);
        List<PriorityBean> priorities = dao.listPriorities(common.projectId, bug.getPriorityId());
        Map<String, Object> params = new HashMap<>();
        common.putAll(params);
        params.put("bug", bug);
        params.put("attachments", attachments);
        params.put("priorities", priorities);
        TemplateUtil.process("editbug.ftl", params, resp.getWriter());
    }

    @Override
    public String post(Context ctx, HttpServletRequest req) throws Exception {
        BugViewDao vdao = new BugViewDao(ctx.connection);
        BugBean bug = vdao.loadBug(bugId, common);
        if (bug == null)
            throw new NoAccessException("Bug " + bugId + " not found", HttpServletResponse.SC_NOT_FOUND);
        BugEditDao dao = new BugEditDao(ctx.connection);
        Integer[] changeBox = new Integer[1];
        UploadUtil<Void> util = new UploadUtil<>(parameters -> {
            BugData data = BugData.create(dao, common, parameters);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(FILE_KEY_START)) {
                    String value = entry.getValue();
                    if (!Boolean.parseBoolean(value)) {
                        int attachmentId = AccessUtil.parseInt(key.substring(FILE_KEY_START.length()));
                        int changeId = dao.getChangeId(bugId, common.getUserId(), changeBox);
                        dao.removeBugAttachment(changeId, attachmentId);
                    }
                }
            }
            dao.changeBug(bugId, common.getUserId(), changeBox, data.priorityId, data.title, data.safeHtml);
            return null;
        });
        util.post(req, (result, fileName, content) -> {
            int attachmentId = dao.addBugAttachment(bugId, fileName, content);
            int changeId = dao.getChangeId(bugId, common.getUserId(), changeBox);
            dao.addBugAttachmentChange(changeId, attachmentId);
        });
        ctx.connection.commit();
        return bug.getViewLink();
    }
}
