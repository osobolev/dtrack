package btrack.dao;

public final class ProjectBean {

    private final int id;
    private final String name;
    private final String description;
    private final String projectRoot;

    public ProjectBean(int id, String name, String description, String projectRoot) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.projectRoot = projectRoot;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getViewLink() {
        return getProjectBase(projectRoot, name);
    }

    public static String getProjectBase(String projectRoot, String projectName) {
        return projectRoot + "/" + projectName;
    }
}
