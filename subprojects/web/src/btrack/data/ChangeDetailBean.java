package btrack.data;

import java.util.List;

public final class ChangeDetailBean {

    private final String fieldChange;
    private final String fileChange;
    private final List<AttachmentBean> changedFiles;

    public ChangeDetailBean(String fieldChange, String fileChange, List<AttachmentBean> changedFiles) {
        this.fieldChange = fieldChange;
        this.fileChange = fileChange;
        this.changedFiles = changedFiles;
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
