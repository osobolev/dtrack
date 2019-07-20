package btrack.dao;

import btrack.data.LinkFactory;
import btrack.data.ReportBean;

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

    public Integer getReportId(int projectId, int num) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id" +
            "  from reports" +
            " where project_id = ?" +
            "   and visible_id = ?"
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

    private static List<ReportBean> loadReportsWithoutQuery(LinkFactory linkFactory, PreparedStatement stmt) throws SQLException {
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

    public List<ReportBean> listReports(int projectId, LinkFactory linkFactory) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, visible_id, name" +
            "  from reports" +
            " where project_id = ?" +
            " order by visible_id"
        )) {
            stmt.setInt(1, projectId);
            return loadReportsWithoutQuery(linkFactory, stmt);
        }
    }

    public List<ReportBean> listFavouriteReports(int projectId, int userId, LinkFactory linkFactory) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, visible_id, name" +
            "  from reports" +
            " where project_id = ?" +
            "   and id in (select report_id from user_reports where user_id = ?)" +
            " order by visible_id"
        )) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);
            return loadReportsWithoutQuery(linkFactory, stmt);
        }
    }

    public void addFavouriteReport(int userId, int reportId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "insert into user_reports" +
            " (user_id, report_id)" +
            " values" +
            " (?, ?)"
        )) {
            stmt.setInt(1, userId);
            stmt.setInt(2, reportId);
            executeUpdate(stmt);
        }
    }

    public void removeFavouriteReport(int userId, int reportId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "delete from user_reports" +
            " where user_id = ?" +
            "   and report_id = ?"
        )) {
            stmt.setInt(1, userId);
            stmt.setInt(2, reportId);
            executeUpdate(stmt);
        }
    }

    public ReportBean loadReport(int reportId, LinkFactory linkFactory) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "select id, visible_id, name, simple_query, json_query" +
            "  from reports" +
            " where id = ?"
        )) {
            stmt.setInt(1, reportId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                int id = rs.getInt(1);
                int num = rs.getInt(2);
                String name = rs.getString(3);
                String simpleQuery = rs.getString(4);
                String jsonQuery = rs.getString(5);
                return new ReportBean(id, num, name, simpleQuery, jsonQuery, linkFactory);
            }
        }
    }
}
