package btrack.dao;

import btrack.actions.ValidationException;
import org.h2.util.ScriptReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// todo: move all validation outside of DAO!!!
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
        private T oldValue;
        final Getter<T> getter;
        final Setter<T> setter;

        FieldUpdate(String field, String oldField, String newField, T newValue, Getter<T> getter, Setter<T> setter) {
            this.field = field;
            this.oldField = oldField;
            this.newField = newField;
            this.newValue = newValue;
            this.getter = getter;
            this.setter = setter;
        }

        FieldUpdate(String field, T newValue, Getter<T> getter, Setter<T> setter) {
            this(field, "old_" + field, "new_" + field, newValue, getter, setter);
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

    private void updateBugFields(int bugId, int userId, Integer[] changeBox,
                                 List<FieldUpdate<?>> fields) throws SQLException {
        {
            String updateFields = fields.stream().map(uf -> uf.field + " = ?").collect(Collectors.joining(", "));
            String selectInternal = fields.stream().map(uf -> uf.field).collect(Collectors.joining(", "));
            String selectExternal = fields.stream().map(uf -> "ob." + uf.field).collect(Collectors.joining(", "));
            try (PreparedStatement stmt = connection.prepareStatement(
                "update bugs nb" +
                "   set " + updateFields + ", modified = current_timestamp, modify_user_id = ?" +
                "  from (select id, " + selectInternal + " from bugs where id = ? for update) ob" +
                " where nb.id = ob.id" +
                " returning " + selectExternal
            )) {
                {
                    int index = 1;
                    for (FieldUpdate<?> update : fields) {
                        update.setNewValue(stmt, index++);
                    }
                    stmt.setInt(index++, userId);
                    stmt.setInt(index++, bugId);
                }
                stmt.execute();
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        int index = 1;
                        for (FieldUpdate<?> update : fields) {
                            update.get(rs, index++);
                        }
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
    }

    private <T> void updateBugField(int bugId, int userId, Integer[] changeBox,
                                    String field, T newValue,
                                    Getter<T> getter, Setter<T> setter) throws SQLException {
        updateBugFields(
            bugId, userId, changeBox,
            Collections.singletonList(new FieldUpdate<>(field, newValue, getter, setter))
        );
    }

    public void changeBug(int bugId, int userId, Integer[] changeBox,
                          int newPriorityId, String shortText, String fullText) throws SQLException {
        updateBugFields(
            bugId, userId, changeBox,
            Arrays.asList(
                new FieldUpdate<>("priority_id", newPriorityId, BaseDao::getInt, BaseDao::setInt),
                new FieldUpdate<>("short_text", shortText, ResultSet::getString, PreparedStatement::setString),
                new FieldUpdate<>("full_text", fullText, ResultSet::getString, PreparedStatement::setString)
            )
        );
    }

    public void changeAssignedUser(int bugId, int userId, Integer[] changeBox,
                                   Integer newAssignedUserId) throws SQLException {
        if (newAssignedUserId != null) {
//            if (!userHasAccess(projectId, newAssignedUserId.intValue())) {
//                // todo: move outside of DAO
//                throw new NoAccessException("No access", HttpServletResponse.SC_FORBIDDEN);
//            }
        }
        updateBugFields(
            bugId, userId, changeBox,
            Collections.singletonList(
                new FieldUpdate<>("assigned_user_id", newAssignedUserId, BaseDao::getInt, BaseDao::setInt)
            )
        );
    }

    // todo: make it boolean
    private void validateState(int projectId, int stateId) throws SQLException, ValidationException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id" +
            "  from states" +
            " where project_id = ?" +
            "   and id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, stateId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ValidationException("Неверно задано состояние");
                }
            }
        }
    }

    public void changeBugState(int projectId, int bugId, int userId, Integer[] changeBox,
                               int newStateId) throws SQLException, ValidationException {
        validateState(projectId, newStateId); // todo: move outside of DAO
        // todo: validate state transition validity
        updateBugField(
            bugId, userId, changeBox,
            "state_id", newStateId,
            BaseDao::getInt, BaseDao::setInt
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

    public void removeBugAttachment(int changeId, int attachmentId, int userId) throws SQLException {
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

    public void runScript(Path script) throws SQLException, IOException {
        if (testing)
            return;
        try (ScriptReader reader = new ScriptReader(Files.newBufferedReader(script, StandardCharsets.UTF_8))) {
            try (Statement stmt = connection.createStatement()) {
                while (true) {
                    String sql = reader.readStatement();
                    if (sql == null)
                        break;
                    stmt.execute(sql);
                }
            }
        }
    }
}
