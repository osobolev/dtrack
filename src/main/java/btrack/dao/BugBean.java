package btrack.dao;

import btrack.AccessUtil;
import btrack.ProjectItem;

public final class BugBean {

    private final String project;
    private final int bugNum;
    private final String title;
    private final String html;
    private final int priorityId;
    private final String priority;
    private final String created;
    private final String createdBy;
    private final String lastUpdated;
    private final String lastUpdatedBy;
    private final int stateId;
    private final String state;
    private final Integer assignedUserId;
    private final String assignedUser;
    private final String projectBase;

    public BugBean(String project, int bugNum, String title, String html, int priorityId, String priority, String created, String createdBy, String lastUpdated, String lastUpdatedBy, int stateId, String state, Integer assignedUserId, String assignedUser, String projectBase) {
        this.project = project;
        this.bugNum = bugNum;
        this.title = title;
        this.html = html;
        this.priorityId = priorityId;
        this.priority = priority;
        this.created = created;
        this.createdBy = createdBy;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedBy = lastUpdatedBy;
        this.stateId = stateId;
        this.state = state;
        this.assignedUserId = assignedUserId;
        this.assignedUser = assignedUser;
        this.projectBase = projectBase;
    }

    public String getProject() {
        return project;
    }

    public int getBugNum() {
        return bugNum;
    }

    public String getTitle() {
        return title;
    }

    public String getHtml() {
        return html;
    }

    public int getPriorityId() {
        return priorityId;
    }

    public String getPriority() {
        return priority;
    }

    public String getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public int getStateId() {
        return stateId;
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
        return assignedUserId != null && assignedUserId.intValue() == user.getId();
    }

    public String getViewLink() {
        return AccessUtil.getItemUrl(projectBase, ProjectItem.BUG, bugNum, null);
    }

    public String getEditLink() {
        return AccessUtil.getItemUrl(projectBase, ProjectItem.BUG, bugNum, "edit.html");
    }

    public String getCommentLink() {
        return AccessUtil.getItemUrl(projectBase, ProjectItem.BUG, bugNum, "comment.html");
    }

    public String getAssignLink() {
        return AccessUtil.getItemUrl(projectBase, ProjectItem.BUG, bugNum, "assign.html");
    }

    public String getAttachmentLink(AttachmentBean attachment) {
        return AccessUtil.getItemUrl(projectBase, ProjectItem.FILE, attachment.getId(), attachment.getName());
    }

    public String getCommentAttachmentLink(AttachmentBean attachment) {
        return AccessUtil.getItemUrl(projectBase, ProjectItem.CFILE, attachment.getId(), attachment.getName());
    }
}
