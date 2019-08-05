package dtrack.admin.json;

public final class ProjectState {

    public final String code;
    public final String name;
    public final Boolean isDefault;

    public ProjectState(String code, String name, Boolean isDefault) {
        this.code = code;
        this.name = name;
        this.isDefault = isDefault;
    }
}
