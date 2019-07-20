package btrack;

import java.sql.Connection;
import java.sql.SQLException;

interface ConnectionProducer {

    Connection getConnection() throws SQLException;
}
