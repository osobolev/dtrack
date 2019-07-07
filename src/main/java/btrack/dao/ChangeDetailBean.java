package btrack.dao;

import java.util.List;

public final class ChangeDetailBean {

    private final String commentHtml;
    private final List<AttachmentBean> commentAttachments;
    private final String fieldChange;
    private final String fileChange;
    private final List<AttachmentBean> changedFiles;

    public ChangeDetailBean(String commentHtml, List<AttachmentBean> commentAttachments, String fieldChange, String fileChange, List<AttachmentBean> changedFiles) {
        this.commentHtml = commentHtml;
        this.commentAttachments = commentAttachments;
        this.fieldChange = fieldChange;
        this.fileChange = fileChange;
        this.changedFiles = changedFiles;
    }

    public String getCommentHtml() {
        return commentHtml;
    }

    public List<AttachmentBean> getCommentAttachments() {
        return commentAttachments;
    }

    public String getFieldChange() {
        return fieldChange;
    }

    public String getFileChange() {
        return fileChange;
    }

    public List<AttachmentBean> getChangedFiles() {
        return changedFiles;
    }
}
