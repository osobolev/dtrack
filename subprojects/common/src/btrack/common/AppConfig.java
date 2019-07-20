package btrack.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public final class AppConfig {

    public static final String DRIVER_CLASS = "org.postgresql.Driver";

    public final String jdbcUrl;
    public final String jdbcUser;
    public final String jdbcPassword;
    public final int httpPort;
    public final String templatesRoot;
    public final String webRoot;

    public AppConfig(String jdbcUrl, String jdbcUser, String jdbcPassword, int httpPort, String templatesRoot, String webRoot) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
        this.httpPort = httpPort;
        this.templatesRoot = templatesRoot;
        this.webRoot = webRoot;
    }

    public static AppConfig load() throws IOException {
        Properties props = new Properties();
        Path path = Paths.get("btrack.properties");
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            props.load(reader);
        }

        String jdbcUrl = props.getProperty("jdbc.url");
        String jdbcUser = props.getProperty("jdbc.user");
        String jdbcPassword = props.getProperty("jdbc.password");

        int port = Integer.parseInt(props.getProperty("http.port", "8080"));
        String templatesRoot = props.getProperty("templates.root");
        String root = props.getProperty("http.root", "src/main/webapp");
        return new AppConfig(jdbcUrl, jdbcUser, jdbcPassword, port, templatesRoot, root);
    }

    public static byte[] hash(String login, String password) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update((login + password).getBytes(StandardCharsets.UTF_8));
            return sha256.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
