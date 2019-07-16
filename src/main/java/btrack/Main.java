package btrack;

import btrack.actions.TemplateUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class Main {

    public static void main(String[] args) {
        // todo: logger impl!!! for production!!!
        Logger logger = LoggerFactory.getLogger(Main.class);
        try {
            Properties props = new Properties();
            Path path = Paths.get("btrack.properties");
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                props.load(reader);
            }

            TemplateUtil.init(props.getProperty("templates.root"));

            HikariConfig config = new HikariConfig();
            config.setMaximumPoolSize(10);
            config.setDriverClassName("org.postgresql.Driver");
            config.setJdbcUrl(props.getProperty("jdbc.url"));
            config.setUsername(props.getProperty("jdbc.user"));
            config.setPassword(props.getProperty("jdbc.password"));
            config.setAutoCommit(false);
            config.setConnectionTestQuery("select 1");
            HikariPool pool = new HikariPool(config);

            ConnectionProducer dataSource = pool::getConnection;

            int port = Integer.parseInt(props.getProperty("http.port", "8080"));
            Server server = new Server(port);
            ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            String root = props.getProperty("http.root", "src/main/webapp");
            handler.setResourceBase(root);
            handler.addServlet(new ServletHolder(new RouterServlet(dataSource)), "/p/*");
            handler.addServlet(new ServletHolder(new LoginServlet(dataSource)), "/login.html");
            handler.addServlet(new ServletHolder(new LogoutServlet()), "/logout.html");
            handler.addServlet(new ServletHolder(new RootServlet(dataSource)), "");
            handler.addServlet(DefaultServlet.class, "/");
            server.setHandler(handler);
            server.start();
        } catch (Throwable ex) {
            logger.error("Fatal error", ex);
            System.exit(1);
        }
    }
}
