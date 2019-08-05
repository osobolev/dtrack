package dtrack.admin.dao;

import dtrack.common.dao.BaseDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    public String getProjectDescription(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select description from projects where id = ?"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                return rs.getString(1);
            }
        }
    }

    public List<String> listProjects() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select name from projects order by 1"
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

    public List<UserDTO> listProjectAccess(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, login from users where id in (select user_id from user_access where project_id = ?) order by 1"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<UserDTO> list = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String login = rs.getString(2);
                    list.add(new UserDTO(id, login));
                }
                return list;
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

    public List<StateDTO> listStates(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select s.code, s.name, ps.is_default, ps.order_num" +
            "  from project_states ps, states s" +
            " where ps.code = s.code" +
            "   and ps.project_id = ?" +
            " order by order_num"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<StateDTO> list = new ArrayList<>();
                while (rs.next()) {
                    String code = rs.getString(1);
                    String name = rs.getString(2);
                    boolean isDefault = rs.getBoolean(3);
                    list.add(new StateDTO(code, name, isDefault));
                }
                return list;
            }
        }
    }

    public List<PriorityDTO> listPriorities(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select p.code, p.name, p.color, pp.is_default, pp.order_num" +
            "  from project_priorities pp, priorities p" +
            " where pp.code = p.code" +
            "   and pp.project_id = ?" +
            " order by order_num"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<PriorityDTO> list = new ArrayList<>();
                while (rs.next()) {
                    String code = rs.getString(1);
                    String name = rs.getString(2);
                    String color = rs.getString(3);
                    boolean isDefault = rs.getBoolean(4);
                    list.add(new PriorityDTO(code, name, color, isDefault));
                }
                return list;
            }
        }
    }

    public Map<String, List<TransitionDTO>> listTransitions(int projectId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select code_from, code_to, name from transitions where project_id = ? order by code_from, code_to"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                Map<String, List<TransitionDTO>> result = new TreeMap<>();
                while (rs.next()) {
                    String from = rs.getString(1);
                    String to = rs.getString(2);
                    String name = rs.getString(3);
                    result.computeIfAbsent(from, k -> new ArrayList<>()).add(new TransitionDTO(to, name));
                }
                return result;
            }
        }
    }

    public List<ReportDTO> listReports(int projectId, boolean needQueries) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select visible_id, name" + (needQueries ? ", simple_query, json_query" : "") +
            "  from reports" +
            " where project_id = ?" +
            " order by visible_id"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ReportDTO> list = new ArrayList<>();
                while (rs.next()) {
                    int num = rs.getInt(1);
                    String name = rs.getString(2);
                    String simple;
                    String json;
                    if (needQueries) {
                        simple = rs.getString(3);
                        json = rs.getString(4);
                    } else {
                        simple = null;
                        json = null;
                    }
                    list.add(new ReportDTO(num, name, simple, json));
                }
                return list;
            }
        }
    }
}
