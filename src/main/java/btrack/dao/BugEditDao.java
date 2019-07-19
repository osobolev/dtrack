package btrack.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BugEditDao extends BaseDao {

    public BugEditDao(Connection connection) {
        super(connection);
    }

    public boolean validatePriority(int projectId, int priorityId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id" +
            "  from priorities" +
            " where project_id = ?" +
            "   and id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, priorityId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Integer getDefaultState(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id" +
            "  from states" +
            " where project_id = ?" +
            "   and is_default"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rs.getInt(1);
            }
        }
    }

    public int getNextBugId(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "update projects" +
            "   set last_visible_id = last_visible_id + 1" +
            " where id = ?" +
            " returning last_visible_id"
        )) {
            stmt.setInt(1, projectId);
            stmt.execute();
            try (ResultSet rs = stmt.getResultSet()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int newBug(int projectId, int userId, int num, int priorityId, int stateId, String shortText, String fullText) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into bugs" +
            " (project_id, visible_id, create_user_id, modify_user_id, state_id, priority_id, short_text, full_text)" +
            " values" +
            " (?, ?, ?, ?, ?, ?, ?, ?)",
            new String[] {"id"}
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, num);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, stateId);
            stmt.setInt(6, priorityId);
            stmt.setString(7, shortText);
            stmt.setString(8, fullText);
            executeUpdate(stmt);
            return getGeneratedId(stmt);
        }
    }

    public int addBugAttachment(int bugId, String fileName, InputStream fileContent) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into bug_attachments" +
            " (bug_id, file_name, file_content)" +
            " values" +
            " (?, ?, ?)",
            new String[] {"id"}
        )) {
            stmt.setInt(1, bugId);
            stmt.setString(2, fileName);
            stmt.setBinaryStream(3, fileContent);
            executeUpdate(stmt);
            return getGeneratedId(stmt);
        }
    }

    private int addBugChange(int bugId, int userId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into changes" +
            " (bug_id, user_id)" +
            " values" +
            " (?, ?)",
            new String[] {"id"}
        )) {
            stmt.setInt(1, bugId);
            stmt.setInt(2, userId);
            executeUpdate(stmt);
            return getGeneratedId(stmt);
        }
    }

    private interface Getter<T> {

        T get(ResultSet rs, int index) throws SQLException;
    }

    private interface Setter<T> {

        void set(PreparedStatement stmt, int index, T value) throws SQLException;
    }

    private static final class FieldUpdate<T> {

        final String field;
        final String oldField;
        final String newField;
        final T newValue;
        final boolean hasPrerequisite;
        final T prerequisite;
        private T oldValue;
        final Getter<T> getter;
        final Setter<T> setter;

        FieldUpdate(String field, String oldField, String newField, T newValue,
                    boolean hasPrerequisite, T prerequisite, Getter<T> getter, Setter<T> setter) {
            this.field = field;
            this.oldField = oldField;
            this.newField = newField;
            this.newValue = newValue;
            this.hasPrerequisite = hasPrerequisite;
            this.prerequisite = prerequisite;
            this.getter = getter;
            this.setter = setter;
        }

        FieldUpdate(String field, T newValue, boolean hasPrerequisite, T prerequisite, Getter<T> getter, Setter<T> setter) {
            this(field, "old_" + field, "new_" + field, newValue, hasPrerequisite, prerequisite, getter, setter);
        }

        FieldUpdate(String field, T newValue, Getter<T> getter, Setter<T> setter) {
            this(field, newValue, false, null, getter, setter);
        }

        void get(ResultSet rs, int index) throws SQLException {
            oldValue = getter.get(rs, index);
        }

        void setNewValue(PreparedStatement stmt, int index) throws SQLException {
            setter.set(stmt, index, newValue);
        }

        void setOldValue(PreparedStatement stmt, int index) throws SQLException {
            setter.set(stmt, index, oldValue);
        }

        void setWhere(PreparedStatement stmt, int index) throws SQLException {
            setter.set(stmt, index, prerequisite);
        }

        boolean isUpdated() {
            return !Objects.equals(oldValue, newValue);
        }
    }

    public int getChangeId(int bugId, int userId, Integer[] changeBox) throws SQLException {
        if (changeBox[0] == null) {
            int id = addBugChange(bugId, userId);
            changeBox[0] = id;
            return id;
        } else {
            return changeBox[0].intValue();
        }
    }

    private boolean updateBugFields(int bugId, int userId, Integer[] changeBox,
                                    List<FieldUpdate<?>> fields) throws SQLException {
        {
            String updateFields = fields.stream().map(uf -> uf.field + " = ?").collect(Collectors.joining(", "));
            String selectInternal = fields.stream().map(uf -> uf.field).collect(Collectors.joining(", "));
            String selectExternal = fields.stream().map(uf -> "ob." + uf.field).collect(Collectors.joining(", "));
            String prerequisites = fields.stream()
                .filter(uf -> uf.hasPrerequisite)
                .map(uf -> " and nb." + uf.field + (uf.prerequisite != null ? " = ?" : " is null"))
                .collect(Collectors.joining());
            String sql = "update bugs nb" +
                         "   set " + updateFields + ", modified = current_timestamp, modify_user_id = ?" +
                         "  from (select id, " + selectInternal + " from bugs where id = ? for update) ob" +
                         " where nb.id = ob.id" + prerequisites +
                         " returning " + selectExternal;
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                {
                    int index = 1;
                    for (FieldUpdate<?> update : fields) {
                        update.setNewValue(stmt, index++);
                    }
                    stmt.setInt(index++, userId);
                    stmt.setInt(index++, bugId);
                    for (FieldUpdate<?> update : fields) {
                        if (update.prerequisite != null) {
                            update.setWhere(stmt, index++);
                        }
                    }
                }
                stmt.execute();
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        int index = 1;
                        for (FieldUpdate<?> update : fields) {
                            update.get(rs, index++);
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        List<FieldUpdate<?>> updated = fields.stream().filter(FieldUpdate::isUpdated).collect(Collectors.toList());
        if (!updated.isEmpty()) {
            int id = getChangeId(bugId, userId, changeBox);
            String insertFields = updated.stream().map(uf -> uf.oldField + ", " + uf.newField).collect(Collectors.joining(", "));
            String insertValues = updated.stream().map(uf -> "?, ?").collect(Collectors.joining(", "));
            try (PreparedStatement stmt = connection.prepareStatement(
                "insert into changes_fields" +
                " (change_id, " + insertFields + ")" +
                " values" +
                " (?, " + insertValues + ")"
            )) {
                int index = 1;
                stmt.setInt(index++, id);
                for (FieldUpdate<?> update : updated) {
                    update.setOldValue(stmt, index++);
                    update.setNewValue(stmt, index++);
                }
                executeUpdate(stmt);
            }
        }
        return true;
    }

    public boolean changeBug(int bugId, int userId, Integer[] changeBox,
                             int newPriorityId, String shortText, String fullText) throws SQLException {
        return updateBugFields(
            bugId, userId, changeBox,
            Arrays.asList(
                new FieldUpdate<>("priority_id", newPriorityId, BaseDao::getInt, BaseDao::setInt),
                new FieldUpdate<>("short_text", shortText, ResultSet::getString, PreparedStatement::setString),
                new FieldUpdate<>("full_text", fullText, ResultSet::getString, PreparedStatement::setString)
            )
        );
    }

    public boolean changeAssignedUser(int bugId, int userId, Integer[] changeBox,
                                      Integer oldUserId, Integer newUserId) throws SQLException {
        return updateBugFields(
            bugId, userId, changeBox,
            Collections.singletonList(
                new FieldUpdate<>("assigned_user_id", newUserId, true, oldUserId, BaseDao::getInt, BaseDao::setInt)
            )
        );
    }

    public boolean validateTransition(int projectId, int fromId, int toId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select project_id" +
            "  from transitions" +
            " where project_id = ?" +
            "   and from_id = ?" +
            "   and to_id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, fromId);
            stmt.setInt(3, toId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean changeBugState(int bugId, int userId, Integer[] changeBox,
                                  int fromStateId, int toStateId) throws SQLException {
        return updateBugFields(
            bugId, userId, changeBox,
            Collections.singletonList(new FieldUpdate<>("state_id", toStateId, true, fromStateId, BaseDao::getInt, BaseDao::setInt))
        );
    }

    public int addBugComment(int bugId, int userId, String comment) throws SQLException {
        int id = addBugChange(bugId, userId);
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into changes_comments" +
            " (change_id, comment_text)" +
            " values" +
            " (?, ?)"
        )) {
            stmt.setInt(1, id);
            stmt.setString(2, comment);
            executeUpdate(stmt);
            return id;
        }
    }

    public void addCommentAttachment(int changeId, String fileName, InputStream fileContent) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into comment_attachments" +
            " (change_id, file_name, file_content)" +
            " values" +
            " (?, ?, ?)"
        )) {
            stmt.setInt(1, changeId);
            stmt.setString(2, fileName);
            stmt.setBinaryStream(3, fileContent);
            executeUpdate(stmt);
        }
    }

    public void deleteComment(int changeId, int userId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "update changes_comments set delete_ts = current_timestamp, delete_user_id = ? where change_id = ?"
        )) {
            stmt.setInt(1, userId);
            stmt.setInt(2, changeId);
            executeUpdate(stmt);
        }
    }

    public void removeBugAttachment(int changeId, int attachmentId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into changes_files" +
            " (change_id, old_attachment_id)" +
            " values" +
            " (?, ?)"
        )) {
            stmt.setInt(1, changeId);
            stmt.setInt(2, attachmentId);
            executeUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "update bug_attachments set is_deleted = true where id = ?"
        )) {
            stmt.setInt(1, attachmentId);
            executeUpdate(stmt);
        }
    }

    public void addBugAttachmentChange(int changeId, int attachmentId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into changes_files" +
            " (change_id, new_attachment_id)" +
            " values" +
            " (?, ?)"
        )) {
            stmt.setInt(1, changeId);
            stmt.setInt(2, attachmentId);
            executeUpdate(stmt);
        }
    }
}
