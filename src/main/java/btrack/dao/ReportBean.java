package btrack.dao;

import btrack.ProjectItem;

public final class ReportBean {

    private final int id;
    private final int reportNum;
    private final String title;
    private final LinkFactory linkFactory;

    public ReportBean(int id, int reportNum, String title, LinkFactory linkFactory) {
        this.id = id;
        this.reportNum = reportNum;
        this.title = title;
        this.linkFactory = linkFactory;
    }

    public String getReportNum() {
        return String.valueOf(reportNum);
    }

    public String getTitle() {
        return title;
    }

    public String getViewLink() {
        return linkFactory.getItemUrl(ProjectItem.REPORT, reportNum, null);
    }
}
