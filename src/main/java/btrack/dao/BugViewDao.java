package btrack.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public final class BugViewDao extends BaseDao {

    public BugViewDao(Connection connection) {
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

    public List<PriorityBean> listPriorities(int projectId, Integer defaultId) throws SQLException {
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
                    boolean isDefault;
                    if (defaultId != null) {
                        isDefault = defaultId.intValue() == id;
                    } else {
                        isDefault = rs.getBoolean(3);
                    }
                    priorities.add(new PriorityBean(id, name, isDefault));
                }
                return priorities;
            }
        }
    }

    public BugBean loadBug(String project, int bugId, int bugNum, String projectBase) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select b.created, b.modified, uc.login, um.login," +
            "       b.state_id, s.name, b.priority_id, p.name, b.assigned_user_id, ua.login," +
            "       b.short_text, b.full_text" +
            "  from bugs b" +
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
                Timestamp created = rs.getTimestamp(1);
                Timestamp modified = rs.getTimestamp(2);
                String createdBy = rs.getString(3);
                String modifiedBy = rs.getString(4);
                int stateId = rs.getInt(5);
                String state = rs.getString(6);
                int priorityId = rs.getInt(7);
                String priority = rs.getString(8);
                Integer assignedUserId = getInt(rs, 9);
                String assigned = rs.getString(10);
                String title = rs.getString(11);
                String html = rs.getString(12);
                return new BugBean(
                    project, bugNum, title, html, priorityId, priority,
                    created.toLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), createdBy,
                    modified.toLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), modifiedBy,
                    stateId, state, assignedUserId, assigned, projectBase
                );
            }
        }
    }

    public List<AttachmentBean> listBugAttachments(int bugId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, file_name" +
            "  from bug_attachments" +
            " where bug_id = ?" +
            "   and not is_deleted" +
            " order by file_name"
        )) {
            stmt.setInt(1, bugId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<AttachmentBean> result = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String fileName = rs.getString(2);
                    result.add(new AttachmentBean(id, fileName));
                }
                return result;
            }
        }
    }

    public List<ChangeBean> loadBugHistory(int bugId) throws SQLException {
        return new ChangeBuilder(connection).loadBugHistory(bugId);
    }

    public void listPossibleAssignees(int projectId, Integer toSkip, List<UserBean> result) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select u.id, u.login" +
            "  from users u" +
            " where u.id in (select user_id from user_access where project_id = ?)" +
            " order by login"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    if (toSkip != null && toSkip.intValue() == id)
                        continue;
                    String login = rs.getString(2);
                    result.add(new UserBean(id, login));
                }
            }
        }
    }

    public boolean loadAttachment(boolean comment, int id, OutputStream os) throws SQLException, IOException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select file_content" +
            "  from " + (comment ? "comment_attachments" : "bug_attachments") +
            " where id = ?"
        )) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    try (InputStream is = rs.getBinaryStream(1)) {
                        byte[] buf = new byte[8192];
                        while (true) {
                            int read = is.read(buf);
                            if (read < 0)
                                break;
                            os.write(buf, 0, read);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public List<TransitionBean> listTransitions(int projectId, int fromId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select t.to_id, t.name, s.order_num" +
            "  from transitions t" +
            "       join states s on s.id = t.to_id" +
            " where t.project_id = ?" +
            "   and t.from_id = ?" +
            " order by order_num"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, fromId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<TransitionBean> result = new ArrayList<>();
                while (rs.next()) {
                    int toId = rs.getInt(1);
                    String name = rs.getString(2);
                    result.add(new TransitionBean(toId, name));
                }
                return result;
            }
        }
    }
}
