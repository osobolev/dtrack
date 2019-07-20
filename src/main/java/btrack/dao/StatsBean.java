package btrack.dao;

public final class StatsBean {

    private final String state;
    private final int count;

    public StatsBean(String state, int count) {
        this.state = state;
        this.count = count;
    }

    public String getState() {
        return state;
    }

    public String getCount() {
        return String.valueOf(count);
    }
}
