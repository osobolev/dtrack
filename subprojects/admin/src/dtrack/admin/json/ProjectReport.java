package dtrack.admin.json;

public final class ProjectReport {

    public final String name;
    public final String simple;
    public final Object json;

    public ProjectReport(String name, String simple, Object json) {
        this.name = name;
        this.simple = simple;
        this.json = json;
    }
}
