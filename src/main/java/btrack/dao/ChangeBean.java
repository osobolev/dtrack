package btrack.dao;

import java.util.List;

public final class ChangeBean {

    private final int id;
    private final String user;
    private final String ts;
    private final List<ChangeDetailBean> details;

    public ChangeBean(int id, String user, String ts, List<ChangeDetailBean> details) {
        this.id = id;
        this.user = user;
        this.ts = ts;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getTs() {
        return ts;
    }

    public List<ChangeDetailBean> getDetails() {
        return details;
    }
}
