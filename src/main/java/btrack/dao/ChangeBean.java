package btrack.dao;

import java.time.LocalDateTime;
import java.util.List;

public final class ChangeBean {

    private final int id;
    private final String user;
    private final LocalDateTime ts;
    private final List<CommentDetailBean> comments;
    private final List<ChangeDetailBean> details;
    private final LinkFactory linkFactory;

    public ChangeBean(int id, String user, LocalDateTime ts, List<CommentDetailBean> comments, List<ChangeDetailBean> details, LinkFactory linkFactory) {
        this.id = id;
        this.user = user;
        this.ts = ts;
        this.comments = comments;
        this.details = details;
        this.linkFactory = linkFactory;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getUser() {
        return user;
    }

    public String getTs() {
        return linkFactory.localDate(ts);
    }

    public List<CommentDetailBean> getComments() {
        return comments;
    }

    public List<ChangeDetailBean> getDetails() {
        return details;
    }
}
