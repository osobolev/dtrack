package btrack.dao;

public final class PriorityBean {

    private final int id;
    private final String name;
    private final String color;
    private final boolean isDefault;

    public PriorityBean(int id, String name, String color, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }

    public String getId() {
        return String.valueOf(id);
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
