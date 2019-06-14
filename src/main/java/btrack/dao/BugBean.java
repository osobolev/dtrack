package btrack.dao;

public final class BugBean {

    private final String project;
    private final int id;
    private final String title;
    private final String html;
    private final String priority;
    private final String created;
    private final String createdBy;
    private final String lastUpdated;
    private final String lastUpdatedBy;
    private final String state;
    // todo: add bug history

    public BugBean(String project, int id, String title, String html, String priority, String created, String createdBy, String lastUpdated, String lastUpdatedBy, String state) {
        this.project = project;
        this.id = id;
        this.title = title;
        this.html = html;
        this.priority = priority;
        this.created = created;
        this.createdBy = createdBy;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedBy = lastUpdatedBy;
        this.state = state;
    }

    public String getProject() {
        return project;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getHtml() {
        return html;
    }

    public String getPriority() {
        return priority;
    }

    public String getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public String getState() {
        return state;
    }
}
