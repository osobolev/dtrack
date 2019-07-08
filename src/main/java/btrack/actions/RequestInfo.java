package btrack.actions;

import btrack.dao.DateFormatter;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

public class RequestInfo implements DateFormatter {

    private final String webRoot;
    private final Locale clientLocale;

    public RequestInfo(String webRoot, Locale clientLocale) {
        this.webRoot = webRoot;
        this.clientLocale = clientLocale;
    }

    public static Locale getClientLocale(HttpServletRequest req) {
        Locale locale = req.getLocale();
        if ("en".equalsIgnoreCase(locale.getLanguage())) {
            return Locale.forLanguageTag("ru");
        }
        return locale;
    }

    public static String getWebRoot(HttpServletRequest req) {
        return req.getContextPath();
    }

    public String getWebRoot() {
        return webRoot;
    }

    @Override
    public final String localDate(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(clientLocale));
    }

    void putTo(Map<String, Object> params) {
        params.put("request", this);
    }
}
