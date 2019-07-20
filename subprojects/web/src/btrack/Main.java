package btrack;

import btrack.actions.TemplateUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class Main {

    public static void runServer(Logger logger, boolean debug) {
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
            handler.addServlet(new ServletHolder(new RouterServlet(logger, dataSource, debug)), "/p/*");
            handler.addServlet(new ServletHolder(new LoginServlet(logger, dataSource, debug)), "/login.html");
            handler.addServlet(new ServletHolder(new LogoutServlet()), "/logout.html");
            handler.addServlet(new ServletHolder(new RootServlet(logger, dataSource)), "");
            handler.addServlet(DefaultServlet.class, "/");
            server.setHandler(handler);
            server.start();
            logger.info("Server started on port " + port + ", web root " + root);
        } catch (Throwable ex) {
            logger.error(ex);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Logger logger = new FileLogger("btrack.log");
        runServer(logger, false);
    }
}
