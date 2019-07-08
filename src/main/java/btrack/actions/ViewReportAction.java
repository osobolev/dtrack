package btrack.actions;

import btrack.dao.BugBean;
import btrack.dao.BugViewDao;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ViewReportAction extends Action {

    private final CommonInfo common;

    public ViewReportAction(CommonInfo common) {
        this.common = common;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        BugViewDao dao = new BugViewDao(ctx.connection);
        List<BugBean> bugs = dao.listAllBugs(common.projectId, common);
        Map<String, Object> params = new HashMap<>();
        common.putAll(params);
        params.put("bugs", bugs);
        TemplateUtil.process("buglist.ftl", params, resp.getWriter());
    }
}
