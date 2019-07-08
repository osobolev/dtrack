package btrack.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ReportDao extends BaseDao {

    public ReportDao(Connection connection) {
        super(connection);
    }

    public List<ReportBean> listReports(int projectId, LinkFactory linkFactory) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, visible_id, name" +
            "  from reports" +
            " where project_id = ?"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ReportBean> result = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    int num = rs.getInt(2);
                    String name = rs.getString(3);
                    result.add(new ReportBean(id, num, name, linkFactory));
                }
                return result;
            }
        }
    }

    public ReportBean loadReport(int projectId, int num, LinkFactory linkFactory) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, name" +
            "  from reports" +
            " where project_id = ?" +
            "   and visible_id = ?"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, num);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                int id = rs.getInt(1);
                String name = rs.getString(2);
                return new ReportBean(id, num, name, linkFactory);
            }
        }
    }

    public void report(int projectId, String sql) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSetMetaData rsmd = stmt.getMetaData();
            if (rsmd.getColumnCount() == 1 && "bug_id".equalsIgnoreCase(rsmd.getColumnName(1))) {
                // todo: add standard columns: num, title, created, modified, assigned, state, priority (pseudo-column - display as color)
            }
            // todo: 2nd special case: group + bug_id - same shit but with grouping
            // todo: always limit to project
            try (ResultSet rs = stmt.executeQuery()) {
                rs.getMetaData();
            }
        }
    }
}
