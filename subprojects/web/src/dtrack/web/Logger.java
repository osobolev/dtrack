package dtrack.web;

public interface Logger {

    void error(Throwable ex);

    void info(String message);
}
