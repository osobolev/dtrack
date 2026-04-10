package dtrack.web;

import dtrack.web.actions.RequestInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserInfo.logout(req, resp);
        String webRoot = RequestInfo.getWebRoot(req);
        resp.sendRedirect(webRoot + "/login.html");
    }
}
