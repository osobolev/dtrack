package btrack.common.dao;

import java.sql.*;

public abstract class BaseDao {

    public static boolean testing = false;

    protected final Connection connection;

    protected BaseDao(Connection connection) {
        this.connection = connection;
    }

    public static Integer getInt(ResultSet rs, int column) throws SQLException {
        int value = rs.getInt(column);
        if (rs.wasNull())
            return null;
        return value;
    }

    public static void setInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value.intValue());
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    public static int getGeneratedId(PreparedStatement stmt) throws SQLException {
        if (testing) {
            return 1;
        } else {
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int executeUpdate(PreparedStatement stmt) throws SQLException {
        if (testing) {
            try {
                stmt.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException ex) {
                // ignore
            } catch (SQLException ex) {
                String sqlState = ex.getSQLState();
                if (sqlState == null || !sqlState.startsWith("23"))
                    throw ex;
                connection.rollback();
            }
            return 0;
        } else {
            return stmt.executeUpdate();
        }
    }
}
