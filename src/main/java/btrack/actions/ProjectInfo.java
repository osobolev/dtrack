package btrack.actions;

import btrack.ProjectItem;
import btrack.UserInfo;
import btrack.dao.LinkFactory;
import btrack.dao.ProjectBean;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ProjectInfo extends LoginInfo implements LinkFactory {

    public final int projectId;
    public final String projectName;
    private final String projectBase;
    // todo: add list of reports for current project

    public ProjectInfo(String webRoot, Locale clientLocale, UserInfo user, List<ProjectBean> availableProjects,
                       int projectId, String projectName, String projectBase) {
        super(webRoot, clientLocale, user, availableProjects);
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectBase = projectBase;
    }

    public String getProjectName() {
        return projectName;
    }

    private String getProjectUrl(String page) {
        return projectBase + (page == null ? "" : "/" + page);
    }

    public String getNewBugUrl() {
        return getProjectUrl("newbug.html");
    }

    public String getReportRootUrl() {
        return getProjectUrl(null);
    }

    public String getBugUrl(int bugNum) {
        return getItemUrl(ProjectItem.BUG, bugNum, null);
    }

    @Override
    public String getItemUrl(ProjectItem item, int num, String page) {
        return projectBase + "/" + item.name().toLowerCase() + "/" + num + (page == null ? "" : "/" + page);
    }

    @Override
    void putTo(Map<String, Object> params) {
        super.putTo(params);
        params.put("project", this);
    }
}
