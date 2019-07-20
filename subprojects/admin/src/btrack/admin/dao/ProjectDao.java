package btrack.admin.dao;

import btrack.common.dao.BaseDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ProjectDao extends BaseDao {

    public ProjectDao(Connection connection) {
        super(connection);
    }

    public Integer findProject(String name) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id from projects where name = ?"
        )) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                return rs.getInt(1);
            }
        }
    }

    public int addProject(String name, String description) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into projects (name, description) values (?, ?)",
            new String[] {"id"}
        )) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            executeUpdate(stmt);
            return getGeneratedId(stmt);
        }
    }

    public void removeProject(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from projects where id = ?"
        )) {
            stmt.setInt(1, projectId);
            executeUpdate(stmt);
        }
    }

    public void setProjectDescription(int projectId, String description) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "update projects set description = ? where id = ?"
        )) {
            stmt.setString(1, description);
            stmt.setInt(2, projectId);
            executeUpdate(stmt);
        }
    }

    public void cloneProject(int fromId, int toId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into project_states" +
            "  (project_id, code, order_num, is_default)" +
            "  select ?, code, order_num, is_default" +
            "    from project_states" +
            "   where project_id = ?"
        )) {
            stmt.setInt(1, toId);
            stmt.setInt(2, fromId);
            executeUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into transitions" +
            "  (project_id, code_from, code_to, name)" +
            "   select ?, code_from, code_to, name" +
            "     from transitions" +
            "    where project_id = ?"
        )) {
            stmt.setInt(1, toId);
            stmt.setInt(2, fromId);
            executeUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into project_priorities" +
            "  (project_id, code, order_num, is_default)" +
            "  select ?, code, order_num, is_default" +
            "    from project_priorities" +
            "   where project_id = ?"
        )) {
            stmt.setInt(1, toId);
            stmt.setInt(2, fromId);
            executeUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into reports" +
            "  (project_id, visible_id, name, simple_query, json_query)" +
            "  select ?, visible_id, name, simple_query, json_query" +
            "    from reports" +
            "   where project_id = ?"
        )) {
            stmt.setInt(1, toId);
            stmt.setInt(2, fromId);
            executeUpdate(stmt);
        }
    }

    public void cleanProject(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from transitions where project_id = ?"
        )) {
            stmt.setInt(1, projectId);
            executeUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from user_states where project_id = ?"
        )) {
            stmt.setInt(1, projectId);
            executeUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from project_states where project_id = ?"
        )) {
            stmt.setInt(1, projectId);
            executeUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from project_priorities where project_id = ?"
        )) {
            stmt.setInt(1, projectId);
            executeUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from reports where project_id = ?"
        )) {
            stmt.setInt(1, projectId);
            executeUpdate(stmt);
        }
    }

    public void addProjectState(int projectId, String stateCode, int orderNum, boolean isDefault) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into project_states" +
            "  (project_id, code, order_num, is_default)" +
            "  values" +
            "  (?, ?, ?, ?)"
        )) {
            stmt.setInt(1, projectId);
            stmt.setString(2, stateCode);
            stmt.setInt(3, orderNum);
            stmt.setBoolean(4, isDefault);
            executeUpdate(stmt);
        }
    }

    public void addProjectPriority(int projectId, String priorityCode, int orderNum, boolean isDefault) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into project_priorities" +
            "  (project_id, code, order_num, is_default)" +
            "  values" +
            "  (?, ?, ?, ?)"
        )) {
            stmt.setInt(1, projectId);
            stmt.setString(2, priorityCode);
            stmt.setInt(3, orderNum);
            stmt.setBoolean(4, isDefault);
            executeUpdate(stmt);
        }
    }

    public void addTransition(int projectId, String from, String to, String name) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into transitions" +
            "  (project_id, code_from, code_to, name)" +
            "  values" +
            "  (?, ?, ?, ?)"
        )) {
            stmt.setInt(1, projectId);
            stmt.setString(2, from);
            stmt.setString(3, to);
            stmt.setString(4, name);
            executeUpdate(stmt);
        }
    }

    public void addReport(int projectId, String name, int orderNum, String simple, String json) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into reports" +
            "  (project_id, visible_id, name, simple_query, json_query)" +
            "  values" +
            "  (?, ?, ?, ?, ?)"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, orderNum);
            stmt.setString(3, name);
            stmt.setString(4, simple);
            stmt.setString(5, json);
            executeUpdate(stmt);
        }
    }

    public Set<String> loadStates() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select code from states"
        )) {
            try (ResultSet rs = stmt.executeQuery()) {
                Set<String> codes = new HashSet<>();
                while (rs.next()) {
                    codes.add(rs.getString(1));
                }
                return codes;
            }
        }
    }

    public void addState(String code, String name) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into states" +
            "  (code, name)" +
            "  values" +
            "  (?, ?)"
        )) {
            stmt.setString(1, code);
            stmt.setString(2, name);
            executeUpdate(stmt);
        }
    }

    public Set<String> loadPriorities() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select code from priorities"
        )) {
            try (ResultSet rs = stmt.executeQuery()) {
                Set<String> codes = new HashSet<>();
                while (rs.next()) {
                    codes.add(rs.getString(1));
                }
                return codes;
            }
        }
    }

    public void addPriority(String code, String name, String color) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into priorities" +
            "  (code, name, color)" +
            "  values" +
            "  (?, ?, ?)"
        )) {
            stmt.setString(1, code);
            stmt.setString(2, name);
            stmt.setString(3, color);
            executeUpdate(stmt);
        }
    }

    public Map<Integer, String> loadUserStates(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select user_id, default_state_code from user_states where project_id = ?"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, String> map = new HashMap<>();
                while (rs.next()) {
                    int userId = rs.getInt(1);
                    String stateCode = rs.getString(2);
                    map.put(userId, stateCode);
                }
                return map;
            }
        }
    }
}
