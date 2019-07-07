package btrack.dao;

import btrack.actions.NoAccessException;
import btrack.actions.ValidationException;
import org.h2.util.ScriptReader;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

// todo: move all validation outside of DAO!!!
public final class BugsDao extends BaseDao {

    public BugsDao(Connection connection) {
        super(connection);
    }

    public Integer getProjectId(String projectName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select p.id" +
            "  from projects p" +
            " where p.name = ?"
        )) {
            stmt.setString(1, projectName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                return rs.getInt(1);
            }
        }
    }

    public Integer getBugId(int projectId, int num) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select b.id" +
            "  from bugs b" +
            " where b.project_id = ?" +
            "   and b.visible_id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, num);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                return rs.getInt(1);
            }
        }
    }

    public boolean userHasAccess(int projectId, int userId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select user_id" +
            "  from user_access" +
            " where project_id = ?" +
            "   and user_id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<PriorityBean> listPriorities(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, name, is_default" +
            "  from priorities" +
            " where project_id = ?" +
            " order by order_num"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<PriorityBean> priorities = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);
                    boolean isDefault = rs.getBoolean(3);
                    priorities.add(new PriorityBean(id, name, isDefault));
                }
                return priorities;
            }
        }
    }

    public BugBean loadBug(int bugId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select pr.name, b.visible_id, b.created, b.modified, uc.login, um.login, ua.login, s.name, p.name, " +
            "       b.short_text, b.full_text" +
            "  from bugs b" +
            "       join projects pr on b.project_id = pr.id" +
            "       join users uc on b.create_user_id = uc.id" +
            "       join users um on b.modify_user_id = um.id" +
            "       left join users ua on b.assigned_user_id = ua.id" +
            "       join states s on b.state_id = s.id" +
            "       join priorities p on b.priority_id = p.id" +
            " where b.id = ?"
        )) {
            stmt.setInt(1, bugId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                String project = rs.getString(1);
                int id = rs.getInt(2);
                Timestamp created = rs.getTimestamp(3);
                Timestamp modified = rs.getTimestamp(4);
                String createdBy = rs.getString(5);
                String modifiedBy = rs.getString(6);
                String assigned = rs.getString(7);
                String state = rs.getString(8);
                String priority = rs.getString(9);
                String title = rs.getString(10);
                String html = rs.getString(11);
                return new BugBean(
                    project, id, title, html, priority,
                    created.toLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), createdBy,
                    modified.toLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), modifiedBy,
                    state
                );
            }
        }
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

    public void addBugAttachment(int bugId, String fileName, InputStream fileContent) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into bug_attachments" +
            " (bug_id, file_name, file_content)" +
            " values" +
            " (?, ?, ?)"
        )) {
            stmt.setInt(1, bugId);
            stmt.setString(2, fileName);
            stmt.setBinaryStream(3, fileContent);
            executeUpdate(stmt);
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

    private void updateBugFields(int bugId, int userId,
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
        int id = addBugChange(bugId, userId);
        List<FieldUpdate<?>> updated = fields.stream().filter(FieldUpdate::isUpdated).collect(Collectors.toList());
        if (!updated.isEmpty()) {
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

    private <T> void updateBugField(int bugId, int userId,
                                    String field, T newValue,
                                    Getter<T> getter, Setter<T> setter) throws SQLException {
        updateBugFields(
            bugId, userId,
            Collections.singletonList(new FieldUpdate<>(field, newValue, getter, setter))
        );
    }

    public void changeBug(int projectId, int bugId, int userId,
                          Integer newAssignedUserId, int newPriorityId, String shortText, String fullText) throws SQLException, ValidationException, NoAccessException {
        if (newAssignedUserId != null) {
            if (!userHasAccess(projectId, newAssignedUserId.intValue())) {
                // todo: move outside of DAO
                throw new NoAccessException("No access", HttpServletResponse.SC_FORBIDDEN);
            }
        }
        if (!validatePriority(projectId, newPriorityId)) {
            throw new ValidationException("Неверно задан приоритет"); // todo: move outside of DAO
        }
        updateBugFields(
            bugId, userId,
            Arrays.asList(
                new FieldUpdate<>("assigned_user_id", newAssignedUserId, BugsDao::getInt, BugsDao::setInt),
                new FieldUpdate<>("priority_id", newPriorityId, BugsDao::getInt, BugsDao::setInt),
                new FieldUpdate<>("short_text", shortText, ResultSet::getString, PreparedStatement::setString),
                new FieldUpdate<>("full_text", fullText, ResultSet::getString, PreparedStatement::setString)
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

    public void changeBugState(int projectId, int bugId, int userId, int newStateId) throws SQLException, ValidationException {
        validateState(projectId, newStateId); // todo: move outside of DAO
        // todo: validate state transition validity
        updateBugField(
            bugId, userId,
            "state_id", newStateId,
            BugsDao::getInt, BugsDao::setInt
        );
    }

    public void addBugComment(int bugId, int userId, String comment) throws SQLException {
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
        }
    }

//    public void removeBugAttachment(int attachmentId, int userId) throws SQLException, ValidationException {
//        try (PreparedStatement stmt = connection.prepareStatement(
//            "select b.id" +
//            "  from user_access ua, bug_attachments ba, bugs b" +
//            " where ba.id = ?" +
//            "   and ba.bug_id = b.id" +
//            "   and b.project_id = ua.project_id" +
//            "   and ua.user_id = ?"
//        )) {
//            stmt.setInt(1, attachmentId);
//            stmt.setInt(2, userId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (!rs.next()) {
//                    return;
//                }
//            }
//        }
//        int bugId;
//        try (PreparedStatement stmt = connection.prepareStatement(
//            "update bug_attachments" +
//            "   set is_deleted = true" +
//            " where id = ?" +
//            " returning bug_id"
//        )) {
//            stmt.setInt(1, attachmentId);
//            stmt.execute();
//            try (ResultSet rs = stmt.getResultSet()) {
//                if (rs.next()) {
//                    bugId = rs.getInt(1);
//                } else {
//                    return;
//                }
//            }
//        }
//        try (PreparedStatement stmt = connection.prepareStatement(
//            "insert into bug_changes" +
//            " (bug_id, old_attachment_id, user_id)" +
//            " values" +
//            " (?, ?, ?)"
//        )) {
//            stmt.setInt(1, bugId);
//            stmt.setInt(2, attachmentId);
//            stmt.setInt(3, userId);
//            executeUpdate(stmt);
//        }
//    }

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
