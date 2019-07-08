package btrack.dao;

import java.sql.*;

public final class ReportDao extends BaseDao {

    public ReportDao(Connection connection) {
        super(connection);
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
