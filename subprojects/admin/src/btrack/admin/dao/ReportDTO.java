package btrack.admin.dao;

public final class ReportDTO {

    public final int num;
    public final String name;
    public final String simple;
    public final String json;

    public ReportDTO(int num, String name, String simple, String json) {
        this.num = num;
        this.name = name;
        this.simple = simple;
        this.json = json;
    }
}
