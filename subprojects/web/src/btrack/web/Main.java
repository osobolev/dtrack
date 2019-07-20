package btrack.web;

import btrack.common.AppConfig;
import btrack.web.actions.TemplateUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public final class Main {

    public static void runServer(Logger logger, boolean debug) {
        try {
            AppConfig appConfig = AppConfig.load();

            TemplateUtil.init(appConfig.templatesRoot);

            HikariConfig config = new HikariConfig();
            config.setMaximumPoolSize(10);
            config.setDriverClassName(AppConfig.DRIVER_CLASS);
            config.setJdbcUrl(appConfig.jdbcUrl);
            config.setUsername(appConfig.jdbcUser);
            config.setPassword(appConfig.jdbcPassword);
            config.setAutoCommit(false);
            config.setConnectionTestQuery("select 1");
            HikariPool pool = new HikariPool(config);

            ConnectionProducer dataSource = pool::getConnection;

            int port = appConfig.httpPort;
            Server server = new Server(port);
            ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            String root = appConfig.webRoot;
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
