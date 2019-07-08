package btrack.actions;

import btrack.ProjectItem;
import btrack.UserInfo;
import btrack.dao.LinkFactory;
import btrack.dao.ProjectBean;

import java.util.List;
import java.util.Map;

public final class CommonInfo implements LinkFactory {

    public final int projectId;
    public final String projectName;
    private final String projectBase;
    private final UserInfo user;
    public final List<ProjectBean> availableProjects;
    // todo: add list of reports for current project

    public CommonInfo(int projectId, String projectName, String projectBase, UserInfo user,
                      List<ProjectBean> availableProjects) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectBase = projectBase;
        this.user = user;
        this.availableProjects = availableProjects;
    }

    int getUserId() {
        return user.id;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<ProjectBean> getAvailableProjects() {
        return availableProjects;
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

    void putAll(Map<String, Object> params) {
        params.put("info", this);
        putUser(params, user);
    }

    static void putUser(Map<String, Object> params, UserInfo user) {
        params.put("displayUser", user.displayName);
    }
}
