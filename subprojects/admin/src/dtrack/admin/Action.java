package dtrack.admin;

import java.sql.Connection;

interface Action {

    void perform(Connection connection) throws Exception;
}
