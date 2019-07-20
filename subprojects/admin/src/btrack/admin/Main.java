package btrack.admin;

import btrack.admin.dao.ProjectDao;
import btrack.admin.dao.UserDao;
import btrack.admin.json.Updater;
import btrack.common.AppConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public final class Main {

    private static Map<String, List<String>> parseOptions(List<String> args, String... allowed) {
        Map<String, List<String>> map = new HashMap<>();
        Set<String> allowedSet = new HashSet<>(Arrays.asList(allowed));
        for (int i = 0; i < args.size(); ) {
            String option = args.get(i);
            if (!allowedSet.contains(option)) {
                throw new IllegalArgumentException("Unrecognized option: " + option);
            }
            if (i + 1 < args.size()) {
                map.computeIfAbsent(option, k -> new ArrayList<>()).add(args.get(i + 1));
            } else {
                throw new IllegalArgumentException("Missing argument for option " + option);
            }
            i += 2;
        }
        return map;
    }

    private static int resolveProject(Connection connection, String project) throws SQLException, UsageException {
        ProjectDao dao = new ProjectDao(connection);
        Integer maybeProjectId = dao.findProject(project);
        if (maybeProjectId == null)
            throw new UsageException("Project " + project + " not found");
        return maybeProjectId.intValue();
    }

    private static List<Integer> resolveProjects(Connection connection, List<String> projects) throws SQLException, UsageException {
        List<Integer> projectIds = new ArrayList<>();
        if (projects != null) {
            for (String project : projects) {
                int projectId = resolveProject(connection, project);
                projectIds.add(projectId);
            }
        }
        return projectIds;
    }

    private static String getSingle(Map<String, List<String>> options, String option) throws UsageException {
        List<String> values = options.get(option);
        if (values == null) {
            return null;
        } else if (values.size() == 1) {
            return values.get(0);
        } else {
            return help("Multiple " + option + " options");
        }
    }

    private static String readPassword(Map<String, List<String>> options) throws UsageException {
        String password = getSingle(options, "-W");
        if (password != null)
            return password;
        char[] passChars = System.console().readPassword("Password: ");
        if (passChars == null)
            return help("No password entered");
        return new String(passChars);
    }

    private static Action userAdd(String login, Map<String, List<String>> options) throws UsageException {
        String password = readPassword(options);
        byte[] passHash = AppConfig.hash(login, password);
        List<String> projects = options.get("-p");
        return connection -> {
            UserDao dao = new UserDao(connection);
            List<Integer> projectIds = resolveProjects(connection, projects);
            int userId;
            try {
                userId = dao.addUser(login, passHash);
            } catch (SQLException ex) {
                checkUniqueViolation(ex);
                throw new UsageException("User '" + login + "' already exists");
            }
            for (Integer projectId : projectIds) {
                dao.addUserAccess(userId, projectId.intValue());
            }
        };
    }

    private static void checkUniqueViolation(SQLException ex) throws SQLException {
        String sqlState = ex.getSQLState();
        if (!"23505".equals(sqlState))
            throw ex;
    }

    private static int resolveUser(Connection connection, String login) throws UsageException, SQLException {
        UserDao dao = new UserDao(connection);
        Integer maybeUserId = dao.findUser(login);
        if (maybeUserId == null)
            throw new UsageException("User '" + login + "' not found");
        return maybeUserId.intValue();
    }

    private static Action userRemove(String login) {
        return connection -> {
            UserDao dao = new UserDao(connection);
            int userId = resolveUser(connection, login);
            dao.removeUser(userId);
        };
    }

    private static Action userAccess(String login, List<String> add, List<String> remove) throws UsageException {
        if (!Collections.disjoint(add, remove))
            return help("Added and removed projects intersect");
        return connection -> {
            int userId = resolveUser(connection, login);
            List<Integer> addIds = resolveProjects(connection, add);
            List<Integer> removeIds = resolveProjects(connection, add);
            UserDao dao = new UserDao(connection);
            for (Integer addId : addIds) {
                dao.addUserAccess(userId, addId.intValue());
            }
            for (Integer removeId : removeIds) {
                dao.removeUserAccess(userId, removeId.intValue());
            }
        };
    }

    private static Action userPassword(String login, Map<String, List<String>> options) throws UsageException {
        String password = readPassword(options);
        byte[] passHash = AppConfig.hash(login, password);
        return connection -> {
            UserDao dao = new UserDao(connection);
            int userId = resolveUser(connection, login);
            dao.setUserPassword(userId, passHash);
        };
    }

    private static Action userState(String login, String project, String state) throws UsageException {
        return connection -> {
            UserDao dao = new UserDao(connection);
            int userId = resolveUser(connection, login);
            int projectId = resolveProject(connection, project);
            boolean setEmpty = "-".equals(state);
            if (!setEmpty) {
                if (!dao.isStateAvailable(projectId, state))
                    throw new UsageException("State '" + state + "' is not present in project '" + project + "'");
            }
            dao.removeUserState(userId, projectId);
            if (!setEmpty) {
                dao.addUserState(userId, projectId, state);
            }
        };
    }

    private static Action user(String command, List<String> args) throws UsageException {
        switch (command) {
        case "add": {
            if (args.isEmpty())
                return usage("user add login [{-p project}] [-W password]");
            String login = args.remove(0);
            Map<String, List<String>> options = parseOptions(args, "-p", "-W");
            return userAdd(login, options);
        }
        case "remove": {
            if (args.isEmpty())
                return usage("user remove login");
            String login = args.remove(0);
            parseOptions(args);
            return userRemove(login);
        }
        case "access": {
            if (args.size() >= 2) {
                boolean ok = true;
                String login = args.remove(0);
                List<String> add = new ArrayList<>();
                List<String> remove = new ArrayList<>();
                for (String arg : args) {
                    if (arg.startsWith("+")) {
                        add.add(arg.substring(1));
                    } else if (arg.startsWith("-")) {
                        remove.add(arg.substring(1));
                    } else {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    return userAccess(login, add, remove);
                }
            }
            return usage("user access login {+project|-project}");
        }
        case "password": {
            if (args.isEmpty())
                return usage("user password login [-W password]");
            String login = args.remove(0);
            Map<String, List<String>> options = parseOptions(args, "-W");
            return userPassword(login, options);
        }
        case "state": {
            if (args.size() < 3)
                return usage("user state login project [stateCode|-]");
            String login = args.remove(0);
            String project = args.remove(0);
            String state = args.remove(0);
            return userState(login, project, state);
        }
        }
        return usage("user add|remove|access|password login [options]");
    }

    private static void runJSON(Connection connection, int projectId, String json, Map<Integer, String> userStates) throws IOException, SQLException, UsageException {
        if ("-".equals(json)) {
            Updater.updateProject(connection, projectId, System.in, userStates);
        } else {
            Path path = Paths.get(json);
            if (!Files.exists(path))
                throw new UsageException("File " + json + " not found");
            try (InputStream is = Files.newInputStream(path)) {
                Updater.updateProject(connection, projectId, is, userStates);
            }
        }
    }

    private static Action projectAdd(String name, Map<String, List<String>> options) throws UsageException {
        String description = getSingle(options, "-d");
        String clone = getSingle(options, "-clone");
        String json = getSingle(options, "-json");
        if (clone != null && json != null)
            throw new UsageException("Both -clone and -json present");
        return connection -> {
            ProjectDao dao = new ProjectDao(connection);
            int projectId;
            try {
                projectId = dao.addProject(name, description);
            } catch (SQLException ex) {
                checkUniqueViolation(ex);
                throw new UsageException("Project '" + name + "' already exists");
            }
            if (clone != null) {
                int fromId = resolveProject(connection, clone);
                dao.cloneProject(fromId, projectId);
            } else if (json != null) {
                runJSON(connection, projectId, json, Collections.emptyMap());
            }
        };
    }

    private static Action projectRemove(String name) {
        return connection -> {
            ProjectDao dao = new ProjectDao(connection);
            int projectId = resolveProject(connection, name);
            dao.removeProject(projectId);
        };
    }

    private static Action projectUpdate(String name, Map<String, List<String>> options) throws UsageException {
        String description = getSingle(options, "-d");
        String json = getSingle(options, "-json");
        if (description == null && json == null)
            throw new UsageException("No update options specified");
        return connection -> {
            ProjectDao dao = new ProjectDao(connection);
            int projectId = resolveProject(connection, name);
            if (description != null) {
                dao.setProjectDescription(projectId, description);
            }
            if (json != null) {
                Map<Integer, String> userStates = dao.loadUserStates(projectId);
                dao.cleanProject(projectId);
                runJSON(connection, projectId, json, userStates);
            }
        };
    }

    private static Action project(String command, List<String> args) throws UsageException {
        switch (command) {
        case "add": {
            if (args.isEmpty())
                return usage("project add name [-d description] [-clone origName] [-json file]");
            String project = args.remove(0);
            Map<String, List<String>> options = parseOptions(args, "-d", "-clone", "-json");
            return projectAdd(project, options);
        }
        case "remove": {
            if (args.isEmpty())
                return usage("project remove name");
            String project = args.remove(0);
            parseOptions(args);
            return projectRemove(project);
        }
        case "update": {
            if (args.isEmpty())
                return usage("project update name [-d description] [-json file]");
            String project = args.remove(0);
            Map<String, List<String>> options = parseOptions(args, "-d", "-json");
            return projectUpdate(project, options);
        }
        }
        return usage("project add|remove|update name [options]");
    }

    private static <T> T usage(String help) throws UsageException{
        throw new UsageException("Usage: " + help);
    }

    private static <T> T help(String help) throws UsageException{
        throw new UsageException(help);
    }

    private static Action admin(List<String> args) throws UsageException {
        if (!args.isEmpty()) {
            String group = args.remove(0);
            switch (group) {
            case "user": {
                String command;
                if (!args.isEmpty()) {
                    command = args.remove(0);
                } else {
                    command = "";
                }
                return user(command, args);
            }
            case "project": {
                String command;
                if (!args.isEmpty()) {
                    command = args.remove(0);
                } else {
                    command = "";
                }
                return project(command, args);
            }
            }
        }
        return usage("user|project ...");
    }

    @SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace"})
    public static void main(String[] args) {
        try {
            List<String> argList = new ArrayList<>(Arrays.asList(args));
            Action action = admin(argList);
            AppConfig appConfig = AppConfig.load();
            try (Connection connection = DriverManager.getConnection(appConfig.jdbcUrl, appConfig.jdbcUser, appConfig.jdbcPassword)) {
                connection.setAutoCommit(false);
                try {
                    action.perform(connection);
                } catch (Exception ex) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex2) {
                        ex.addSuppressed(ex2);
                    }
                    throw ex;
                }
                connection.commit();
            }
        } catch (UsageException ue) {
            System.out.println(ue.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
