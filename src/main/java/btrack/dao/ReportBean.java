package btrack.dao;

import btrack.ProjectItem;

public final class ReportBean {

    private final int reportNum;
    private final String title;
    private final LinkFactory linkFactory;

    public ReportBean(int reportNum, String title, LinkFactory linkFactory) {
        this.reportNum = reportNum;
        this.title = title;
        this.linkFactory = linkFactory;
    }

    public int getReportNum() {
        return reportNum;
    }

    public String getTitle() {
        return title;
    }

    public LinkFactory getLinkFactory() {
        return linkFactory;
    }

    public String getViewLink() {
        return linkFactory.getItemUrl(ProjectItem.REPORT, reportNum, null);
    }
}
