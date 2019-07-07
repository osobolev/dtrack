package btrack.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;

public abstract class Action {

    public void get(Context ctx, HttpServletRequest req, Writer out) throws Exception {
        throw new NoAccessException("Not implemented", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public String post(Context ctx, HttpServletRequest req) throws Exception {
        return null;
    }
}
