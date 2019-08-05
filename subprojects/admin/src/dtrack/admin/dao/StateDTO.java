package dtrack.admin.dao;

public final class StateDTO {

    public final String code;
    public final String name;
    public final boolean isDefault;

    public StateDTO(String code, String name, boolean isDefault) {
        this.code = code;
        this.name = name;
        this.isDefault = isDefault;
    }
}
