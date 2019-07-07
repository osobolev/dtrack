package btrack;

import btrack.actions.ValidationException;

import javax.servlet.http.HttpServletRequest;

public final class AccessUtil {

    public static String getProjectBase(HttpServletRequest req, String projectName) {
        return req.getServletPath() + "/" + projectName;
    }

    public static String getProjectUrl(HttpServletRequest req, String projectName, String page) {
        String projectBase = getProjectBase(req, projectName);
        return projectBase + "/" + page;
    }

    public static String getBugUrl(HttpServletRequest req, String projectName, int bugNum) {
        String projectBase = getProjectBase(req, projectName);
        return getItemUrl(projectBase, ProjectItem.BUG, bugNum, null);
    }

    public static String getItemUrl(String projectBase, ProjectItem item, int num, String page) {
        return projectBase + "/" + item.name().toLowerCase() + "/" + num + (page == null ? "" : "/" + page);
    }

    public static int parseInt(String str) throws ValidationException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfex) {
            throw new ValidationException("Not an int: " + str);
        }
    }
}
