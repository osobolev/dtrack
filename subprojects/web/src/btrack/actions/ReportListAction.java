package btrack.actions;

import btrack.dao.BugViewDao;
import btrack.data.ReportBean;
import btrack.dao.ReportDao;
import btrack.data.StatsBean;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReportListAction extends Action {

    private final ProjectInfo request;

    public ReportListAction(ProjectInfo request) {
        this.request = request;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        List<StatsBean> stats = new BugViewDao(ctx.connection).getProjectStats(request.projectId);
        ReportDao dao = new ReportDao(ctx.connection);
        List<ReportBean> reports = dao.listReports(request.projectId, request);
        Map<String, Object> params = new HashMap<>();
        request.putTo(params);
        params.put("stats", stats);
        params.put("reports", reports);
        params.put("skipReports", true);
        TemplateUtil.process("reportlist.ftl", params, resp.getWriter());
    }
}
