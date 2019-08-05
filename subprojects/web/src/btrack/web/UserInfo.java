package btrack.web;

import org.jose4j.lang.JoseException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

public final class UserInfo implements Serializable {

    private static final String ATTRIBUTE = "userInfo";
    private static final String COOKIE = "userInfo";
    private static final String EMPTY_COOKIE = "_";

    public final int id;
    public final String displayName;

    public UserInfo(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    private static Cookie findCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE.equals(cookie.getName()))
                    return cookie;
            }
        }
        return null;
    }

    public static UserInfo get(HttpServletRequest req) {
        UserInfo userInfo = (UserInfo) req.getSession().getAttribute(ATTRIBUTE);
        if (userInfo != null)
            return userInfo;
        Cookie cookie = findCookie(req);
        if (cookie != null) {
            String value = cookie.getValue();
            if (!(value.isEmpty() || EMPTY_COOKIE.equals(value))) {
                try {
                    String token = URLDecoder.decode(value, "UTF-8");
                    KeyStore ks = KeyStore.get();
                    UserInfo cookieInfo = ks.getLoginInfo(token);
                    req.getSession().setAttribute(ATTRIBUTE, cookieInfo);
                    return cookieInfo;
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
        return null;
    }

    public void login(HttpServletRequest req, HttpServletResponse resp, boolean rememberMe) throws InvalidKeySpecException, NoSuchAlgorithmException, JoseException, IOException {
        req.getSession().setAttribute(ATTRIBUTE, this);
        if (rememberMe) {
            KeyStore ks = KeyStore.get();
            String token = ks.createToken(this);
            Cookie cookie = new Cookie(COOKIE, URLEncoder.encode(token, "UTF-8"));
            cookie.setPath("/");
            cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(365));
            cookie.setHttpOnly(true);
            resp.addCookie(cookie);
        }
    }

    public static void logout(HttpServletRequest req, HttpServletResponse resp) {
        req.getSession().removeAttribute(ATTRIBUTE);
        Cookie existing = findCookie(req);
        if (existing != null) {
            Cookie cookie = new Cookie(COOKIE, EMPTY_COOKIE);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            resp.addCookie(cookie);
        }
    }
}
