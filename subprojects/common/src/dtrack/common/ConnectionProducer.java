package dtrack.common;

import java.sql.Connection;

public interface ConnectionProducer {

    Connection getConnection() throws Exception;
}
