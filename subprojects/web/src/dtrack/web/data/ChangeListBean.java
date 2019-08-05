package dtrack.web.data;

import java.util.List;

public final class ChangeListBean {

    private final String id;
    private final String text;
    private final List<ChangeBean> changes;

    public ChangeListBean(String id, String text, List<ChangeBean> changes) {
        this.id = id;
        this.text = text;
        this.changes = changes;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<ChangeBean> getChanges() {
        return changes;
    }
}
