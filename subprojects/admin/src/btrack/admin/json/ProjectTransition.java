package btrack.admin.json;

public final class ProjectTransition {

    public final String from;
    public final String to;
    public final String name;

    public ProjectTransition(String from, String to, String name) {
        this.from = from;
        this.to = to;
        this.name = name;
    }
}
