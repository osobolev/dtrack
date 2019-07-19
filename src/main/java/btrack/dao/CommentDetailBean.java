package btrack.dao;

import btrack.ProjectItem;

import java.time.LocalDateTime;
import java.util.List;

public final class CommentDetailBean {

    private final int bugNum;
    private final int id;
    private final String commentHtml;
    private final List<AttachmentBean> commentAttachments;
    private final LocalDateTime deleted;
    private final String deleteUser;
    private final LinkFactory linkFactory;

    public CommentDetailBean(int bugNum, int id, String commentHtml, List<AttachmentBean> commentAttachments, LocalDateTime deleted, String deleteUser, LinkFactory linkFactory) {
        this.bugNum = bugNum;
        this.id = id;
        this.commentHtml = commentHtml;
        this.commentAttachments = commentAttachments;
        this.deleted = deleted;
        this.deleteUser = deleteUser;
        this.linkFactory = linkFactory;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getCommentHtml() {
        return commentHtml;
    }

    public List<AttachmentBean> getCommentAttachments() {
        return commentAttachments;
    }

    public String getDeleted() {
        return deleted == null ? null : linkFactory.localDate(deleted);
    }

    public String getDeleteUser() {
        return deleteUser;
    }

    public String getDeleteLink() {
        return linkFactory.getItemUrl(ProjectItem.BUG, bugNum, "deleteComment.html");
    }
}
