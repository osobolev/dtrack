package btrack.actions;

import btrack.ProjectItem;
import btrack.UserInfo;
import btrack.dao.LinkFactory;

public final class CommonInfo implements LinkFactory {

    public final int projectId;
    public final String projectName;
    private final String projectBase;
    public final UserInfo user;
    // todo: add list of available projects

    public CommonInfo(int projectId, String projectName, String projectBase, UserInfo user) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectBase = projectBase;
        this.user = user;
    }

    int getUserId() {
        return user.id;
    }

    public String getProjectUrl(String page) {
        return projectBase + "/" + page;
    }

    public String getBugUrl(int bugNum) {
        return getItemUrl(ProjectItem.BUG, bugNum, null);
    }

    @Override
    public String getItemUrl(ProjectItem item, int num, String page) {
        return projectBase + "/" + item.name().toLowerCase() + "/" + num + (page == null ? "" : "/" + page);
    }
}
