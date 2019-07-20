package btrack.data;

public final class UserBean {

    public final int id;
    private final String login;

    public UserBean(int id, String login) {
        this.id = id;
        this.login = login;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getLogin() {
        return login;
    }
}
