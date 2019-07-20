package btrack.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Action {

    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        throw new NoAccessException("Not implemented", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String redirect = post(ctx, req);
        if (redirect == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            resp.sendRedirect(redirect);
        }
    }

    public String post(Context ctx, HttpServletRequest req) throws Exception {
        return null;
    }
}
