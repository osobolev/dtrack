package btrack.actions;

public final class NoAccessException extends Exception {

    public final int code;

    public NoAccessException(String message, int code) {
        super(message);
        this.code = code;
    }
}
