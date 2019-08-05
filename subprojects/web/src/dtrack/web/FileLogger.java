package dtrack.web;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

final class FileLogger implements Logger {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

    private final PrintWriter pw;

    private static PrintWriter pw(OutputStream os, Charset charset) {
        return new PrintWriter(new OutputStreamWriter(os, charset), true);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static PrintWriter open(String fileName) {
        try {
            OutputStream os = Files.newOutputStream(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            PrintWriter fileOutput = pw(os, StandardCharsets.UTF_8);
            start(fileOutput);
            return fileOutput;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return pw(System.out, Charset.defaultCharset());
        }
    }

    private static String getTimestamp() {
        return TIMESTAMP_FORMAT.format(LocalDateTime.now());
    }

    private static void start(PrintWriter pw) {
        String date = getTimestamp();
        pw.println("------------------ Log started: " + date + " ------------------");
    }

    FileLogger(String fileName) {
        this.pw = open(fileName);
    }

    private void printMessage(String type, String message) {
        pw.println("[" + type + "] " + getTimestamp() + (message == null ? "" : " | " + message));
    }

    @Override
    public void error(Throwable ex) {
        printMessage("ERROR", ex.toString());
        ex.printStackTrace(pw);
        pw.println("-------------------------------");
    }

    @Override
    public void info(String message) {
        printMessage("INFO", null);
        pw.println(message);
    }
}
