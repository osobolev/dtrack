package btrack.actions;

import btrack.dao.BugEditDao;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Map;

public final class AddCommentAction extends Action {

    private final int bugId;
    private final int bugNum;
    private final ProjectInfo request;

    public AddCommentAction(int bugId, int bugNum, ProjectInfo request) {
        this.bugId = bugId;
        this.bugNum = bugNum;
        this.request = request;
    }

    private Integer createComment(BugEditDao dao, Map<String, String> parameters) throws ValidationException, SQLException {
        String untrustedHtml = parameters.get("comment");
        if (untrustedHtml == null)
            throw new ValidationException("Missing 'comment' parameter");
        String safeHtml = UploadUtil.POLICY.sanitize(untrustedHtml);
        return dao.addBugComment(bugId, request.getUserId(), safeHtml);
    }

    @Override
    public String post(Context ctx, HttpServletRequest req) throws Exception {
        BugEditDao dao = new BugEditDao(ctx.connection);
        UploadUtil<Integer> util = new UploadUtil<>(parameters -> createComment(dao, parameters));
        util.post(
            req,
            (commentId, fileName, content) -> dao.addCommentAttachment(commentId.intValue(), fileName, content)
        );
        ctx.connection.commit();
        return request.getBugUrl(bugNum);
    }
}
