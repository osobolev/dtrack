package btrack.actions;

import btrack.UserInfo;
import btrack.data.ProjectBean;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoginInfo extends RequestInfo {

    private final UserInfo user;
    private final List<ProjectBean> availableProjects;

    public LoginInfo(String webRoot, Locale clientLocale, UserInfo user, List<ProjectBean> availableProjects) {
        super(webRoot, clientLocale);
        this.user = user;
        this.availableProjects = availableProjects;
    }

    public final List<ProjectBean> getAvailableProjects() {
        return availableProjects;
    }

    final int getUserId() {
        return user.id;
    }

    public String getDisplayUser() {
        return user.displayName;
    }

    void putTo(Map<String, Object> params) {
        super.putTo(params);
        params.put("login", this);
    }
}
