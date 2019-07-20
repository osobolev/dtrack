package btrack.actions;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public final class ProjectListAction extends Action {

    private final LoginInfo request;

    public ProjectListAction(LoginInfo request) {
        this.request = request;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        Map<String, Object> params = new HashMap<>();
        request.putTo(params);
        TemplateUtil.process("projectlist.ftl", params, resp.getWriter());
    }
}
