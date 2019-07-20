package btrack;

import btrack.web.Logger;
import btrack.web.Main;

public final class TestMain {

    public static void main(String[] args) {
        Logger logger = new Logger() {

            @Override
            public void error(Throwable ex) {
                ex.printStackTrace();
            }

            @Override
            public void info(String message) {
                System.out.println(message);
            }
        };
        Main.runServer(logger, true);
    }
}
