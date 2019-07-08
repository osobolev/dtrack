package btrack.dao;

public final class AttachmentBean {

    public final int id;
    private final String name;

    public AttachmentBean(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getName() {
        return name;
    }
}
