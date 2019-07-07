package btrack.dao;

public final class TransitionBean {

    private final int toId;
    private final String name;

    public TransitionBean(int toId, String name) {
        this.toId = toId;
        this.name = name;
    }

    public int getToId() {
        return toId;
    }

    public String getName() {
        return name;
    }
}
