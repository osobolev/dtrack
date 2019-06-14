package btrack;

import btrack.actions.NewBugServlet;
import btrack.actions.ViewBugServlet;
import btrack.dao.BugsDao;
import btrack.dao.ConnectionProducer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.nio.file.Paths;
import java.sql.SQLException;

public final class Main {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler handler = new ServletContextHandler();
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
        try {
            BugsDao dao = new BugsDao(dataSource);
            dao.runScript(Paths.get("sql/tables.sql"));
            dao.runScript(Paths.get("sql/data.sql"));
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        handler.addServlet(new ServletHolder(new NewBugServlet(dataSource)), "/actions/newbug.html");
        handler.addServlet(new ServletHolder(new ViewBugServlet(dataSource)), "/actions/viewbug.html");
        handler.setResourceBase("src/main/webapp");
        handler.addServlet(DefaultServlet.class, "/");
        server.setHandler(handler);
        server.start();
    }
}
