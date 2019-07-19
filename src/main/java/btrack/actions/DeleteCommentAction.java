package btrack.actions;

import btrack.dao.BugEditDao;

import javax.servlet.http.HttpServletRequest;

public final class DeleteCommentAction extends Action {

    private final int bugNum;
    private final ProjectInfo request;

    public DeleteCommentAction(int bugNum, ProjectInfo request) {
        this.bugNum = bugNum;
        this.request = request;
    }

    @Override
    public String post(Context ctx, HttpServletRequest req) throws Exception {
        String commentId = req.getParameter("commentId");
        if (commentId == null)
            throw new ValidationException("Missing 'commentId' parameter");
        int changeId = Context.parseInt(commentId);
        new BugEditDao(ctx.connection).deleteComment(changeId, request.getUserId());
        ctx.connection.commit();
        return request.getBugUrl(bugNum);
    }
}
