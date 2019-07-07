package btrack;

import btrack.actions.ValidationException;

import javax.servlet.http.HttpServletRequest;

public final class AccessUtil {

    public static String getProjectUrl(HttpServletRequest req, String projectName, String page) {
        return req.getContextPath() + "/" + projectName + (page == null ? "" : "/" + page);
    }

    public static String getBugUrl(HttpServletRequest req, String projectName, int bugNum, String page) {
        return req.getContextPath() + "/" + projectName + "/" + bugNum + (page == null ? "" : "/" + page);
    }

    public static int parseInt(String str) throws ValidationException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfex) {
            throw new ValidationException("Not an int: " + str);
        }
    }
}
