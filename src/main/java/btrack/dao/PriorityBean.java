package btrack.dao;

public final class PriorityBean {

    private final int id;
    private final String name;
    private final boolean isDefault;

    public PriorityBean(int id, String name, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.isDefault = isDefault;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
