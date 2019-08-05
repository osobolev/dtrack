package dtrack.admin.dao;

public final class PriorityDTO {

    public final String code;
    public final String name;
    public final String color;
    public final boolean isDefault;

    public PriorityDTO(String code, String name, String color, boolean isDefault) {
        this.code = code;
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }
}
