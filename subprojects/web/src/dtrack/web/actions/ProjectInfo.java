package dtrack.web.actions;

import dtrack.web.UserInfo;
import dtrack.web.data.LinkFactory;
import dtrack.web.data.ProjectBean;
import dtrack.web.data.ProjectItem;
import dtrack.web.data.ReportBean;

import java.util.*;

public final class ProjectInfo extends LoginInfo implements LinkFactory {

    public final int projectId;
    public final String projectName;
    private final String projectBase;
    private final List<ReportBean> favourites = new ArrayList<>();
    private final Set<Integer> favouriteIds = new HashSet<>();

    public ProjectInfo(String webRoot, Locale clientLocale, UserInfo user, List<ProjectBean> availableProjects,
                       int projectId, String projectName, String projectBase) {
        super(webRoot, clientLocale, user, availableProjects);
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectBase = projectBase;
    }

    public void addFavourites(List<ReportBean> reports) {
        favourites.addAll(reports);
        for (ReportBean report : reports) {
            favouriteIds.add(report.id);
        }
    }

    public String getProjectName() {
        return projectName;
    }

    public List<ReportBean> getFavourites() {
        return favourites;
    }

    public boolean isFavourite(ReportBean report) {
        return favouriteIds.contains(report.id);
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
