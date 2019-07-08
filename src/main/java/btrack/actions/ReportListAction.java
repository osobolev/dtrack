package btrack.actions;

import btrack.dao.ReportBean;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
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
        List<ReportBean> reports = Collections.singletonList(new ReportBean(1, "Все баги", request));
        Map<String, Object> params = new HashMap<>();
        request.putTo(params);
        params.put("reports", reports);
        TemplateUtil.process("reportlist.ftl", params, resp.getWriter());
    }
}
