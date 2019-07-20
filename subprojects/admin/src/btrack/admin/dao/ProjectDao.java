package btrack.admin.dao;

import btrack.common.dao.BaseDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            "insert into project_states " +
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
}
