package btrack.actions;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;

public final class Context {

    public final Connection connection;

    public Context(Connection connection) {
        this.connection = connection;
    }

    public static int parseInt(String str) throws ValidationException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfex) {
            throw new ValidationException("Not an int: " + str);
        }
    }

    public static String getWebRoot(HttpServletRequest req) {
        return req.getContextPath();
    }
}
