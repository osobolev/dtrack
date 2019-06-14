package btrack.dao;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.h2.util.ScriptReader;

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

public final class BugsDao {

    private final ConnectionProducer dataSource;

    public BugsDao(ConnectionProducer dataSource) {
        this.dataSource = dataSource;
    }

    private static void validateUserAccess(Connection connection, int projectId, int userId) throws SQLException, ValidationException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select user_id" +
            "  from user_access" +
            " where project_id = ?" +
            "   and user_id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ValidationException("У пользователя нет доступа к проекту");
                }
            }
        }
    }

    private static int checkBugAccess(Connection connection, int bugId, int userId) throws SQLException, ValidationException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select b.project_id" +
            "  from user_access ua, bugs b" +
            " where b.id = ?" +
            "   and b.project_id = ua.project_id" +
            "   and ua.user_id = ?"
        )) {
            stmt.setInt(1, bugId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ValidationException("У вас нет доступа к проекту");
                } else {
                    return rs.getInt(1);
                }
            }
        }
    }

    public List<PriorityBean> listPriorities(int projectId, int userId) throws SQLException, ValidationException {
        try (Connection connection = dataSource.getConnection()) {
            validateUserAccess(connection, projectId, userId);
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
    }

    public BugBean loadBug(int bugId, int userId) throws SQLException, ValidationException {
        try (Connection connection = dataSource.getConnection()) {
            checkBugAccess(connection, bugId, userId);
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
                        throw new ValueException("Баг не найден");
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
    }

    private static int getGeneratedId(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void setInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value.intValue());
        }
    }

    private static Integer getInt(ResultSet rs, int index) throws SQLException {
        int value = rs.getInt(index);
        if (rs.wasNull())
            return null;
        return value;
    }

    private static void validatePriority(Connection connection, int projectId, int priorityId) throws SQLException, ValidationException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id" +
            "  from priorities" +
            " where project_id = ?" +
            "   and id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, priorityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ValidationException("Неверно задан приоритет");
                }
            }
        }
    }

    public int newBug(int projectId, int userId, int priorityId, String shortText, String fullText) throws SQLException, ValidationException {
        try (Connection connection = dataSource.getConnection()) {
            validateUserAccess(connection, projectId, userId);
            validatePriority(connection, projectId, priorityId);
            int stateId;
            try (PreparedStatement stmt = connection.prepareStatement(
                "select id" +
                "  from states" +
                " where project_id = ?" +
                "   and is_default"
            )) {
                stmt.setInt(1, projectId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new ValidationException("Не задано состояние по умолчанию");
                    }
                    stateId = rs.getInt(1);
                }
            }
            int visibleId;
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
                    visibleId = rs.getInt(1);
                }
            }
            int id;
            try (PreparedStatement stmt = connection.prepareStatement(
                "insert into bugs" +
                " (project_id, visible_id, create_user_id, modify_user_id, state_id, priority_id, short_text, full_text)" +
                " values" +
                " (?, ?, ?, ?, ?, ?, ?, ?)",
                new String[] {"id"}
            )) {
                stmt.setInt(1, projectId);
                stmt.setInt(2, visibleId);
                stmt.setInt(3, userId);
                stmt.setInt(4, userId);
                stmt.setInt(5, stateId);
                stmt.setInt(6, priorityId);
                stmt.setString(7, shortText);
                stmt.setString(8, fullText);
                stmt.executeUpdate();
                id = getGeneratedId(stmt);
            }
            connection.commit(); // todo: commit only after all attachments saved???
            return id;
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

    private static void updateBugFields(Connection connection, int bugId, int userId,
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
                    stmt.setInt(index, userId);
                    stmt.setInt(index, bugId);
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
            String insertFields = updated.stream().map(uf -> uf.oldField + ", " + uf.newField).collect(Collectors.joining(", "));
            String insertValues = updated.stream().map(uf -> "?, ?").collect(Collectors.joining(", "));
            try (PreparedStatement stmt = connection.prepareStatement(
                "insert into bug_changes" +
                " (bug_id, user_id, " + insertFields + ")" +
                " values" +
                " (?, ?, " + insertValues + ")"
            )) {
                int index = 1;
                stmt.setInt(index++, bugId);
                stmt.setInt(index++, userId);
                for (FieldUpdate<?> update : updated) {
                    update.setOldValue(stmt, index++);
                    update.setNewValue(stmt, index++);
                }
                stmt.executeUpdate();
            }
        }
    }

    private static <T> void updateBugField(Connection connection, int bugId, int userId,
                                           String field, T newValue,
                                           Getter<T> getter, Setter<T> setter) throws SQLException {
        updateBugFields(
            connection, bugId, userId,
            Collections.singletonList(new FieldUpdate<>(field, newValue, getter, setter))
        );
    }

    public void changeBug(int bugId, int userId,
                          Integer newAssignedUserId, int newPriorityId, String shortText, String fullText) throws SQLException, ValidationException {
        try (Connection connection = dataSource.getConnection()) {
            int projectId = checkBugAccess(connection, bugId, userId);
            if (newAssignedUserId != null) {
                validateUserAccess(connection, projectId, newAssignedUserId.intValue());
            }
            validatePriority(connection, projectId, newPriorityId);
            updateBugFields(
                connection, bugId, userId,
                Arrays.asList(
                    new FieldUpdate<>("assigned_user_id", newAssignedUserId, BugsDao::getInt, BugsDao::setInt),
                    new FieldUpdate<>("priority_id", newPriorityId, BugsDao::getInt, BugsDao::setInt),
                    new FieldUpdate<>("short_text", shortText, ResultSet::getString, PreparedStatement::setString),
                    new FieldUpdate<>("full_text", fullText, ResultSet::getString, PreparedStatement::setString)
                )
            );
            connection.commit();
        }
    }

    private static void validateState(Connection connection, int projectId, int stateId) throws SQLException, ValidationException {
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

    public void changeBugState(int bugId, int userId, int newStateId) throws SQLException, ValidationException {
        try (Connection connection = dataSource.getConnection()) {
            int projectId = checkBugAccess(connection, bugId, userId);
            validateState(connection, projectId, newStateId);
            // todo: validate state transition validity
            updateBugField(
                connection, bugId, userId,
                "state_id", newStateId,
                BugsDao::getInt, BugsDao::setInt
            );
            connection.commit();
        }
    }

    public void addBugComment(int bugId, int userId, String comment) throws SQLException, ValidationException {
        try (Connection connection = dataSource.getConnection()) {
            checkBugAccess(connection, bugId, userId);
            try (PreparedStatement stmt = connection.prepareStatement(
                "insert into bug_changes" +
                " (bug_id, comment_text, user_id)" +
                " values" +
                " (?, ?, ?)"
            )) {
                stmt.setInt(1, bugId);
                stmt.setString(2, comment);
                stmt.setInt(3, userId);
                stmt.executeUpdate();
            }
            connection.commit();
        }
    }

    public void addBugAttachment(int bugId, int userId, String fileName, InputStream fileContent) throws SQLException, ValidationException {
        try (Connection connection = dataSource.getConnection()) {
            checkBugAccess(connection, bugId, userId);
            int attachmentId;
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
                stmt.executeUpdate();
                attachmentId = getGeneratedId(stmt);
            }
            try (PreparedStatement stmt = connection.prepareStatement(
                "insert into bug_changes" +
                " (bug_id, new_attachment_id, user_id)" +
                " values" +
                " (?, ?, ?)"
            )) {
                stmt.setInt(1, bugId);
                stmt.setInt(2, attachmentId);
                stmt.setInt(3, userId);
                stmt.executeUpdate();
            }
            connection.commit();
        }
    }

    public void removeBugAttachment(int attachmentId, int userId) throws SQLException, ValidationException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(
                "select b.id" +
                "  from user_access ua, bug_attachments ba, bugs b" +
                " where ba.id = ?" +
                "   and ba.bug_id = b.id" +
                "   and b.project_id = ua.project_id" +
                "   and ua.user_id = ?"
            )) {
                stmt.setInt(1, attachmentId);
                stmt.setInt(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new ValidationException("У вас нет доступа к проекту");
                    }
                }
            }
            int bugId;
            try (PreparedStatement stmt = connection.prepareStatement(
                "update bug_attachments" +
                "   set is_deleted = true" +
                " where id = ?" +
                " returning bug_id"
            )) {
                stmt.setInt(1, attachmentId);
                stmt.execute();
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        bugId = rs.getInt(1);
                    } else {
                        return;
                    }
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement(
                "insert into bug_changes" +
                " (bug_id, old_attachment_id, user_id)" +
                " values" +
                " (?, ?, ?)"
            )) {
                stmt.setInt(1, bugId);
                stmt.setInt(2, attachmentId);
                stmt.setInt(3, userId);
                stmt.executeUpdate();
            }
            connection.commit();
        }
    }

    public void runScript(Path script) throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection()) {
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
            connection.commit();
        }
    }
}
