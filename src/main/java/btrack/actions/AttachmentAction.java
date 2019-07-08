package btrack.actions;

import btrack.dao.BugViewDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class AttachmentAction extends Action {

    private final boolean comment;
    private final int attachmentId;

    public AttachmentAction(boolean comment, int attachmentId) {
        this.comment = comment;
        this.attachmentId = attachmentId;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        BugViewDao dao = new BugViewDao(ctx.connection);
        if (!dao.loadAttachment(comment, attachmentId, resp.getOutputStream())) {
            throw new NoAccessException((comment ? "Comment attachment" : "Attachment") + " " + attachmentId + " not found", HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
