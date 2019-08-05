package btrack.web.actions;

import btrack.common.AppConfig;
import btrack.web.UserInfo;
import btrack.web.dao.BugViewDao;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class LoginAction extends Action {

    private final boolean debug;
    private final String redirectTo;
    private final RequestInfo request;

    public LoginAction(boolean debug, String redirectTo, RequestInfo request) {
        this.debug = debug;
        this.redirectTo = redirectTo;
        this.request = request;
    }

    private void render(HttpServletResponse resp, String login, boolean remember, String redirect, String error) throws IOException, TemplateException {
        Map<String, Object> params = new HashMap<>();
        params.put("redirect", redirect);
        params.put("error", error);
        params.put("login", login == null ? "" : login);
        params.put("remember", remember);
        request.putTo(params);
        TemplateUtil.process("login.ftl", params, resp.getWriter());
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        render(resp, null, true, redirectTo, null);
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        boolean rememberMe = req.getParameter("rememberMe") != null;
        byte[] passHash = AppConfig.hash(login, password);
        BugViewDao dao = new BugViewDao(ctx.connection);
        Integer userId = dao.checkLogin(debug, login, passHash);
        String redirect = req.getParameter("redirect");
        if (userId == null) {
            render(resp, login, rememberMe, redirect, "Неправильный логин или пароль");
        } else {
            new UserInfo(userId.intValue(), login).login(req, resp, rememberMe);
            String to;
            if (redirect == null) {
                to = request.getWebRoot() + "/";
            } else {
                to = redirect;
            }
            resp.sendRedirect(to);
        }
    }
}
