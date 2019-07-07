package btrack.dao;

public final class UserBean {

    private final int id;
    private final String login;

    public UserBean(int id, String login) {
        this.id = id;
        this.login = login;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }
}
