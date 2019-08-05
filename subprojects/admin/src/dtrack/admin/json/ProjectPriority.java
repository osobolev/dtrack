package dtrack.admin.json;

public final class ProjectPriority {

    public final String code;
    public final String name;
    public final String color;
    public final Boolean isDefault;

    public ProjectPriority(String code, String name, String color, Boolean isDefault) {
        this.code = code;
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }
}
