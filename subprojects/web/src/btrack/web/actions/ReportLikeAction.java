package btrack.web.actions;

import btrack.web.dao.ReportDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class ReportLikeAction extends Action {

    private final int reportId;
    private final boolean like;
    private final ProjectInfo request;

    public ReportLikeAction(int reportId, boolean like, ProjectInfo request) {
        this.reportId = reportId;
        this.like = like;
        this.request = request;
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        System.out.println(reportId + " " + like);
        ReportDao dao = new ReportDao(ctx.connection);
        if (like) {
            dao.addFavouriteReport(request.getUserId(), reportId);
        } else {
            dao.removeFavouriteReport(request.getUserId(), reportId);
        }
        ctx.connection.commit();
    }
}
