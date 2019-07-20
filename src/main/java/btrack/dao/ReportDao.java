package btrack.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            " where project_id = ?" +
            " order by visible_id"
        )) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ReportBean> result = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    int num = rs.getInt(2);
                    String name = rs.getString(3);
                    result.add(new ReportBean(id, num, name, null, null, linkFactory));
                }
                return result;
            }
        }
    }

    public ReportBean loadReport(int projectId, int num, LinkFactory linkFactory) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, name, simple_query, json_query" +
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
                String simpleQuery = rs.getString(3);
                String jsonQuery = rs.getString(4);
                return new ReportBean(id, num, name, simpleQuery, jsonQuery, linkFactory);
            }
        }
    }
}
