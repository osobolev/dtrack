package dtrack.web.data;

public final class PriorityBean {

    private final String code;
    private final String name;
    private final String color;
    private final boolean isDefault;

    public PriorityBean(String code, String name, String color, boolean isDefault) {
        this.code = code;
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }

    public String getId() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
