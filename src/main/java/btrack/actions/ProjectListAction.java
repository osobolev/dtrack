package btrack.actions;

import btrack.UserInfo;
import btrack.dao.ProjectBean;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProjectListAction extends Action {

    private final String webRoot;
    private final List<ProjectBean> availableProjects;
    private final UserInfo user;

    public ProjectListAction(String webRoot, List<ProjectBean> availableProjects, UserInfo user) {
        this.webRoot = webRoot;
        this.availableProjects = availableProjects;
        this.user = user;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("projects", availableProjects);
        CommonInfo.putHeaderData(params, user, webRoot);
        TemplateUtil.process("projectlist.ftl", params, resp.getWriter());
    }
}
