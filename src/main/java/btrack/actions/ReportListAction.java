package btrack.actions;

import btrack.dao.ReportBean;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReportListAction extends Action {

    private final CommonInfo common;

    public ReportListAction(CommonInfo common) {
        this.common = common;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        List<ReportBean> reports = Collections.singletonList(new ReportBean(1, "Все баги", common));
        Map<String, Object> params = new HashMap<>();
        common.putAll(params);
        params.put("reports", reports);
        TemplateUtil.process("reportlist.ftl", params, resp.getWriter());
    }
}
