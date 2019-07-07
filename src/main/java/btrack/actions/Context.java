package btrack.actions;

import java.sql.Connection;

public final class Context {

    public final Connection connection;

    public Context(Connection connection) {
        this.connection = connection;
    }
}
