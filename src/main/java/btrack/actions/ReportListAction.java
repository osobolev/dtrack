package btrack.actions;

import btrack.dao.ReportBean;
import btrack.dao.ReportDao;

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
        ReportDao dao = new ReportDao(ctx.connection);
        List<ReportBean> reports = dao.listReports(request.projectId, request);
        Map<String, Object> params = new HashMap<>();
        request.putTo(params);
        params.put("reports", reports);
        TemplateUtil.process("reportlist.ftl", params, resp.getWriter());
    }
}
