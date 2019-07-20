package btrack.data;

import java.time.LocalDateTime;

public final class BugBean {

    public final int bugNum;
    private final String title;
    private final String html;
    public final String priorityCode;
    private final String priority;
    private final LocalDateTime created;
    private final String createdBy;
    private final LocalDateTime lastUpdated;
    private final String lastUpdatedBy;
    public final String stateCode;
    private final String state;
    private final Integer assignedUserId;
    private final String assignedUser;
    private final LinkFactory linkFactory;

    public BugBean(int bugNum, String title, String html, String priorityCode, String priority, LocalDateTime created, String createdBy, LocalDateTime lastUpdated, String lastUpdatedBy, String stateCode, String state, Integer assignedUserId, String assignedUser, LinkFactory linkFactory) {
        this.bugNum = bugNum;
        this.title = title;
        this.html = html;
        this.priorityCode = priorityCode;
        this.priority = priority;
        this.created = created;
        this.createdBy = createdBy;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedBy = lastUpdatedBy;
        this.stateCode = stateCode;
        this.state = state;
        this.assignedUserId = assignedUserId;
        this.assignedUser = assignedUser;
        this.linkFactory = linkFactory;
    }

    public String getBugNum() {
        return String.valueOf(bugNum);
    }

    public String getTitle() {
        return title;
    }

    public String getHtml() {
        return html;
    }

    public String getPriorityCode() {
        return priorityCode;
    }

    public String getPriority() {
        return priority;
    }

    public String getCreated() {
        return linkFactory.localDate(created);
    }

    public String getCreatedISO() {
        return DateFormatter.isoDate(created);
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastUpdated() {
        return linkFactory.localDate(lastUpdated);
    }

    public String getLastUpdatedISO() {
        return DateFormatter.isoDate(lastUpdated);
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getState() {
        return state;
    }

    public Integer getAssignedUserId() {
        return assignedUserId;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public boolean isNotAssigned() {
        return assignedUserId == null;
    }

    public boolean isAssigned(UserBean user) {
        return assignedUserId != null && assignedUserId.intValue() == user.id;
    }

    public String getViewLink() {
        return linkFactory.getItemUrl(ProjectItem.BUG, bugNum, null);
    }

    public String getEditLink() {
        return linkFactory.getItemUrl(ProjectItem.BUG, bugNum, "edit.html");
    }

    public String getCommentLink() {
        return linkFactory.getItemUrl(ProjectItem.BUG, bugNum, "comment.html");
    }

    public String getAssignLink() {
        return linkFactory.getItemUrl(ProjectItem.BUG, bugNum, "assign.html");
    }

    public String getMoveLink() {
        return linkFactory.getItemUrl(ProjectItem.BUG, bugNum, "move.html");
    }

    public String getAttachmentLink(AttachmentBean attachment) {
        return linkFactory.getItemUrl(ProjectItem.FILE, attachment.id, attachment.getName());
    }

    public String getCommentAttachmentLink(AttachmentBean attachment) {
        return linkFactory.getItemUrl(ProjectItem.CFILE, attachment.id, attachment.getName());
    }
}
