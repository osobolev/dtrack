package btrack;

import btrack.actions.TemplateUtil;
import btrack.dao.BugEditDao;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public final class Main {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);
        try {
            TemplateUtil.init();
            Server server = new Server(8080);
            ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            HikariConfig config = new HikariConfig();
            config.setMaximumPoolSize(10);
            config.setDriverClassName("org.postgresql.Driver");
            config.setJdbcUrl("jdbc:postgresql://localhost:5431/");
            config.setUsername("btrack");
            config.setPassword("btrack");
            config.setAutoCommit(false);
            config.setConnectionTestQuery("select 1");
            HikariPool pool = new HikariPool(config);
            ConnectionProducer dataSource = pool::getConnection;
            try (Connection connection = dataSource.getConnection()) {
                BugEditDao dao = new BugEditDao(connection);
                dao.runScript(Paths.get("sql/tables.sql"));
                dao.runScript(Paths.get("sql/data.sql"));
                connection.commit();
            } catch (SQLException ex) {
                logger.warn(ex.getMessage());
            }
            handler.setResourceBase("src/main/webapp");
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
