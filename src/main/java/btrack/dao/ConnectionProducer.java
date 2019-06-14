package btrack.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProducer {

    Connection getConnection() throws SQLException;
}
