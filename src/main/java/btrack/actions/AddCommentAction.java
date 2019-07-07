package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.BugEditDao;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Map;

public final class AddCommentAction extends Action {

    private final String projectName;
    private final int bugId;
    private final int bugNum;
    private final int userId;

    public AddCommentAction(String projectName, int bugId, int bugNum, int userId) {
        this.projectName = projectName;
        this.bugId = bugId;
        this.bugNum = bugNum;
        this.userId = userId;
    }

    private Integer createComment(BugEditDao dao, Map<String, String> parameters) throws ValidationException, SQLException {
        String untrustedHtml = parameters.get("comment");
        if (untrustedHtml == null)
            throw new ValidationException("Missing 'html' parameter");
        String safeHtml = UploadUtil.POLICY.sanitize(untrustedHtml);
        return dao.addBugComment(bugId, userId, safeHtml);
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
        return AccessUtil.getBugUrl(req, projectName, bugNum);
    }
}
