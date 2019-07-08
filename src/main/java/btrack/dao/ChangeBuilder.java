package btrack.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

final class ChangeBuilder {

    private abstract static class Change {

    }

    private static final class FieldChange extends Change {

        final String change;

        FieldChange(String change) {
            this.change = change;
        }
    }

    private static final class CommentChange extends Change {

        final String comment;
        final List<AttachmentBean> attachments;

        CommentChange(String comment, List<AttachmentBean> attachments) {
            this.comment = comment;
            this.attachments = attachments;
        }
    }

    private static final class FileChange extends Change {

        final boolean filesAdded;
        final List<AttachmentBean> files;

        FileChange(boolean filesAdded, List<AttachmentBean> files) {
            this.filesAdded = filesAdded;
            this.files = files;
        }
    }

    private final Connection connection;
    private final Map<Integer, List<Change>> changes = new HashMap<>();

    ChangeBuilder(Connection connection) {
        this.connection = connection;
    }

    private void addChange(Integer id, Change change) {
        changes.computeIfAbsent(id, k -> new ArrayList<>()).add(change);
    }

    List<ChangeBean> loadBugHistory(int bugId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select cf.change_id," +
            "       au1.login, au2.login," +
            "       s1.name, s2.name," +
            "       p1.name, p2.name," +
            "       cf.new_short_text is not null," +
            "       cf.new_full_text is not null" +
            "  from changes_fields cf " +
            "       left join users au1 on cf.old_assigned_user_id = au1.id" +
            "       left join users au2 on cf.new_assigned_user_id = au2.id" +
            "       left join states s1 on cf.old_state_id = s1.id" +
            "       left join states s2 on cf.new_state_id = s2.id" +
            "       left join priorities p1 on cf.old_priority_id = p1.id" +
            "       left join priorities p2 on cf.new_priority_id = p2.id" +
            " where cf.change_id in (select id from changes where bug_id = ?)"
        )) {
            stmt.setInt(1, bugId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String oldAssigned = rs.getString(2);
                    String newAssigned = rs.getString(3);
                    String oldState = rs.getString(4);
                    String newState = rs.getString(5);
                    String oldPriority = rs.getString(6);
                    String newPriority = rs.getString(7);
                    boolean newTitle = rs.getBoolean(8);
                    boolean newText = rs.getBoolean(9);
                    if (oldAssigned != null || newAssigned != null) {
                        String assignedChange;
                        if (oldAssigned != null && newAssigned != null) {
                            assignedChange = "Исполнитель изменен с '" + oldAssigned + "' на '" + newAssigned + "'"; // todo: add style
                        } else if (oldAssigned != null) {
                            assignedChange = "Удален исполнитель '" + oldAssigned + "'"; // todo: add style
                        } else {
                            assignedChange = "Поставлен исполнитель '" + newAssigned + "'"; // todo: add style
                        }
                        addChange(id, new FieldChange(assignedChange));
                    }
                    if (oldState != null && newState != null) {
                        // todo: add style
                        String stateChange = "Состояние изменено с '" + oldState + "' на '" + newState + "'";
                        addChange(id, new FieldChange(stateChange));
                    }
                    if (oldPriority != null && newPriority != null) {
                        // todo: add style
                        String priorityChange = "Приоритет изменен с '" + oldPriority + "' на '" + newPriority + "'";
                        addChange(id, new FieldChange(priorityChange));
                    }
                    if (newTitle || newText) {
                        addChange(id, new FieldChange("Описание бага изменено"));
                    }
                }
            }
        }
        Map<Integer, List<AttachmentBean>> commentFiles = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
            "select cc.change_id, ca.id, ca.file_name" +
            "  from comment_attachments ca join changes_comments cc on ca.change_id = cc.change_id" +
            " where not cc.is_deleted" +
            "   and cc.change_id in (select id from changes where bug_id = ?)" +
            " order by file_name"
        )) {
            stmt.setInt(1, bugId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    int fileId = rs.getInt(2);
                    String fileName = rs.getString(3);
                    commentFiles.computeIfAbsent(id, k -> new ArrayList<>()).add(new AttachmentBean(fileId, fileName));
                }
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "select cc.change_id, case when cc.is_deleted then null else cc.comment_text end" +
            "  from changes_comments cc" +
            " where cc.change_id in (select id from changes where bug_id = ?)"
        )) {
            stmt.setInt(1, bugId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String comment = rs.getString(2);
                    List<AttachmentBean> attachments = commentFiles.getOrDefault(id, Collections.emptyList());
                    addChange(id, new CommentChange(comment, attachments));
                }
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "select cf.change_id, " +
            "       a1.id, a1.file_name file1, " +
            "       a2.id, a2.file_name file2" +
            "  from changes_files cf" +
            "       left join bug_attachments a1 on a1.id = cf.old_attachment_id" +
            "       left join bug_attachments a2 on a2.id = cf.new_attachment_id" +
            " where cf.change_id in (select id from changes where bug_id = ?)" +
            " order by file1, file2"
        )) {
            stmt.setInt(1, bugId);
            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, List<AttachmentBean>> deleted = new HashMap<>();
                Map<Integer, List<AttachmentBean>> added = new HashMap<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    Integer deletedFileId = BaseDao.getInt(rs, 2);
                    String deletedFileName = rs.getString(3);
                    Integer addedFileId = BaseDao.getInt(rs, 4);
                    String addedFileName = rs.getString(5);
                    if (deletedFileId != null && deletedFileName != null) {
                        AttachmentBean a = new AttachmentBean(deletedFileId.intValue(), deletedFileName);
                        deleted.computeIfAbsent(id, k -> new ArrayList<>()).add(a);
                    }
                    if (addedFileId != null && addedFileName != null) {
                        AttachmentBean a = new AttachmentBean(addedFileId.intValue(), addedFileName);
                        added.computeIfAbsent(id, k -> new ArrayList<>()).add(a);
                    }
                }
                Set<Integer> allChanges = new HashSet<>();
                allChanges.addAll(deleted.keySet());
                allChanges.addAll(added.keySet());
                for (Integer id : allChanges) {
                    List<AttachmentBean> addedFiles = added.get(id);
                    if (addedFiles != null) {
                        addChange(id, new FileChange(true, addedFiles));
                    }
                    List<AttachmentBean> deletedFiles = deleted.get(id);
                    if (deletedFiles != null) {
                        addChange(id, new FileChange(false, deletedFiles));
                    }
                }
            }
        }
        List<ChangeBean> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
            "select c.id, u.login, c.ts" +
            "  from changes c join users u on c.user_id = u.id" +
            " where bug_id = ?" +
            " order by id"
        )) {
            stmt.setInt(1, bugId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    List<Change> change = changes.get(id);
                    if (change == null)
                        continue;
                    String user = rs.getString(2);
                    // todo: use client locale for date formatting!!!
                    String ts = rs.getTimestamp(3).toLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
                    List<ChangeDetailBean> details = new ArrayList<>();
                    for (Change c : change) {
                        ChangeDetailBean detail;
                        if (c instanceof FieldChange) {
                            FieldChange fc = (FieldChange) c;
                            detail = new ChangeDetailBean(
                                null, Collections.emptyList(),
                                fc.change, null, Collections.emptyList()
                            );
                        } else if (c instanceof CommentChange) {
                            CommentChange cc = (CommentChange) c;
                            detail = new ChangeDetailBean(
                                cc.comment, cc.attachments,
                                null, null, Collections.emptyList()
                            );
                        } else if (c instanceof FileChange) {
                            FileChange fc = (FileChange) c;
                            detail = new ChangeDetailBean(
                                null, Collections.emptyList(),
                                null, fc.filesAdded ? "Добавлены файлы" : "Удалены файлы", fc.files
                            );
                        } else {
                            continue;
                        }
                        details.add(detail);
                    }
                    if (details.isEmpty())
                        continue;
                    result.add(new ChangeBean(id, user, ts, details));
                }
            }
        }
        return result;
    }
}
