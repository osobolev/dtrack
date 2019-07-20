package btrack.data;

public final class ReportBean {

    public final int id;
    private final int reportNum;
    private final String title;
    public final String simpleQuery;
    public final String jsonQuery;
    private final LinkFactory linkFactory;

    public ReportBean(int id, int reportNum, String title, String simpleQuery, String jsonQuery, LinkFactory linkFactory) {
        this.id = id;
        this.reportNum = reportNum;
        this.title = title;
        this.simpleQuery = simpleQuery;
        this.jsonQuery = jsonQuery;
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
