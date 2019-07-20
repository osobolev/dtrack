package btrack.admin.dao;

import btrack.common.dao.BaseDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UserDao extends BaseDao {

    public UserDao(Connection connection) {
        super(connection);
    }

    public Integer findUser(String login) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id from users where login = ?"
        )) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                return rs.getInt(1);
            }
        }
    }

    public List<String> listUsers() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select login from users order by 1"
        )) {
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
                return list;
            }
        }
    }

    public List<ProjectDTO> listUserAccess(int userId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, name from projects where id in (select project_id from user_access where user_id = ?) order by 1"
        )) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ProjectDTO> list = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);
                    list.add(new ProjectDTO(id, name));
                }
                return list;
            }
        }
    }

    public Map<Integer, String> loadUserStates(int userId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select project_id, default_state_code from user_states where user_id = ?"
        )) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, String> result = new HashMap<>();
                while (rs.next()) {
                    int projectId = rs.getInt(1);
                    String stateCode = rs.getString(2);
                    result.put(projectId, stateCode);
                }
                return result;
            }
        }
    }

    public int addUser(String login, byte[] passHash) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into users (login, pass_hash) values (?, ?)",
            new String[] {"id"}
        )) {
            stmt.setString(1, login);
            stmt.setBytes(2, passHash);
            executeUpdate(stmt);
            return getGeneratedId(stmt);
        }
    }

    public void removeUser(int userId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from users where id = ?"
        )) {
            stmt.setInt(1, userId);
            executeUpdate(stmt);
        }
    }

    public void setUserPassword(int userId, byte[] passHash) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "update users set pass_hash = ? where id = ?"
        )) {
            stmt.setBytes(1, passHash);
            stmt.setInt(2, userId);
            executeUpdate(stmt);
        }
    }

    public void addUserAccess(int userId, int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into user_access (user_id, project_id) values (?, ?)"
        )) {
            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);
            executeUpdate(stmt);
        }
    }

    public void removeUserAccess(int userId, int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from user_access where user_id = ? and project_id = ?"
        )) {
            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);
            executeUpdate(stmt);
        }
    }

    public void addUserState(int userId, int projectId, String stateCode) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into user_states (project_id, user_id, default_state_code) values (?, ?, ?)"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);
            stmt.setString(3, stateCode);
            executeUpdate(stmt);
        }
    }

    public void removeUserState(int userId, int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from user_states where project_id = ? and user_id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);
            executeUpdate(stmt);
        }
    }

    public boolean isStateAvailable(int projectId, String stateCode) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select code from project_states where project_id = ? and code = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setString(2, stateCode);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
