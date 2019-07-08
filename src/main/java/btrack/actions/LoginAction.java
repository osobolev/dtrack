package btrack.actions;

import btrack.UserInfo;
import btrack.dao.BugViewDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public final class LoginAction extends Action {

    private final String redirectTo;

    public LoginAction(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("redirect", redirectTo);
        params.put("login", "");
        TemplateUtil.process("login.ftl", params, resp.getWriter());
    }

    static byte[] hash(String login, String password) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update((login + password).getBytes(StandardCharsets.UTF_8));
        return sha256.digest();
    }

    @Override
    public void post(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        byte[] passHash = hash(login, password);
        BugViewDao dao = new BugViewDao(ctx.connection);
        Integer userId = dao.checkLogin(login, passHash);
        String redirect = req.getParameter("redirect");
        if (userId == null) {
            Map<String, Object> params = new HashMap<>();
            params.put("redirect", redirect);
            params.put("error", "Неправильный логин или пароль");
            params.put("login", login == null ? "" : login);
            TemplateUtil.process("login.ftl", params, resp.getWriter());
        } else {
            req.getSession().setAttribute(UserInfo.ATTRIBUTE, new UserInfo(userId.intValue(), login));
            String to;
            if (redirect == null) {
                to = "/";
            } else {
                to = redirect;
            }
            resp.sendRedirect(to);
        }
    }
}
