package btrack.web.data;

public final class TransitionBean {

    private final String toCode;
    private final String name;

    public TransitionBean(String toCode, String name) {
        this.toCode = toCode;
        this.name = name;
    }

    public String getToCode() {
        return String.valueOf(toCode);
    }

    public String getName() {
        return name;
    }
}
