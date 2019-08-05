package btrack.web;

import btrack.web.actions.RequestInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

final class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserInfo.logout(req, resp);
        String webRoot = RequestInfo.getWebRoot(req);
        resp.sendRedirect(webRoot + "/login.html");
    }
}
