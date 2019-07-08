package btrack.actions;

import btrack.dao.BugBean;
import btrack.dao.BugViewDao;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ViewReportAction extends Action {

    private final ProjectInfo request;

    public ViewReportAction(ProjectInfo request) {
        this.request = request;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        BugViewDao dao = new BugViewDao(ctx.connection);
        List<BugBean> bugs = dao.listAllBugs(request.projectId, request);
        Map<String, Object> params = new HashMap<>();
        request.putTo(params);
        params.put("bugs", bugs);
        TemplateUtil.process("buglist.ftl", params, resp.getWriter());
    }
}
