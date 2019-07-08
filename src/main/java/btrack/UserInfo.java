package btrack;

import java.io.Serializable;

public final class UserInfo implements Serializable {

    public static final String ATTRIBUTE = "userInfo";

    public final int id;
    public final String displayName;

    public UserInfo(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
