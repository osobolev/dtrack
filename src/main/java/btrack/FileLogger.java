package btrack;

import apploader.common.LogFormatUtil;

import java.io.PrintWriter;

final class FileLogger implements Logger {

    private final PrintWriter pw;

    FileLogger(String fileName) {
        PrintWriter pw = LogFormatUtil.open(fileName);
        this.pw = LogFormatUtil.getWriter(pw);
    }

    @Override
    public void error(Throwable ex) {
        LogFormatUtil.printStackTrace(pw, ex);
    }

    @Override
    public void info(String message) {
        LogFormatUtil.output(pw, "INFO", message);
    }
}
