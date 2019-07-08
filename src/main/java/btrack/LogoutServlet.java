package btrack;

import btrack.actions.Context;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession().removeAttribute(UserInfo.ATTRIBUTE);
        String webRoot = Context.getWebRoot(req);
        resp.sendRedirect(webRoot + "/login.html");
    }
}
