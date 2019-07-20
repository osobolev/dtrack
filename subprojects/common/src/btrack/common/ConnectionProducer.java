package btrack.common;

import java.sql.Connection;

public interface ConnectionProducer {

    Connection getConnection() throws Exception;
}
