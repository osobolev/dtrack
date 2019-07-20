package btrack.admin.dao;

import btrack.common.dao.BaseDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
