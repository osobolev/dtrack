package dtrack.admin.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtrack.admin.UsageException;
import dtrack.admin.dao.ProjectDao;
import dtrack.admin.dao.UserDao;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Importer {

    public static void importProject(Connection connection, int projectId, Reader rdr,
                                     Map<Integer, String> userStates) throws SQLException, UsageException {
        Gson gson = new GsonBuilder().create();
        Project project = gson.fromJson(rdr, Project.class);
        ProjectDao dao = new ProjectDao(connection);
        Set<String> existingStates = dao.loadStates();
        Set<String> existingPriorities = dao.loadPriorities();
        Set<String> projectStates = new HashSet<>();
        int defaultStates = 0;
        for (int i = 0; i < project.states.size(); i++) {
            ProjectState state = project.states.get(i);
            if (state.code == null) {
                throw new UsageException("State code is not specified");
            }
            if (!existingStates.contains(state.code)) {
                if (state.name == null) {
                    throw new UsageException("State '" + state.code + "': name not specified");
                }
                dao.addState(state.code, state.name);
                existingStates.add(state.code);
            }
            boolean isDefault = state.isDefault == null ? false : state.isDefault.booleanValue();
            if (isDefault) {
                defaultStates++;
            }
            dao.addProjectState(projectId, state.code, i + 1, isDefault);
            projectStates.add(state.code);
        }
        if (defaultStates != 1) {
            throw new UsageException("Must be exactly one default state");
        }
        int defaultPriorities = 0;
        for (int i = 0; i < project.priorities.size(); i++) {
            ProjectPriority priority = project.priorities.get(i);
            if (priority.code == null) {
                throw new UsageException("Priority code is not specified");
            }
            if (!existingPriorities.contains(priority.code)) {
                if (priority.name == null) {
                    throw new UsageException("Priority '" + priority.code + "': name not specified");
                }
                if (priority.color == null) {
                    throw new UsageException("Priority '" + priority.code + "': color not specified");
                }
                dao.addPriority(priority.code, priority.name, priority.color);
                existingPriorities.add(priority.code);
            }
            boolean isDefault = priority.isDefault == null ? false : priority.isDefault.booleanValue();
            if (isDefault) {
                defaultPriorities++;
            }
            dao.addProjectPriority(projectId, priority.code, i + 1, isDefault);
        }
        if (defaultPriorities != 1) {
            throw new UsageException("Must be exactly one default priority");
        }
        for (ProjectTransition transition : project.transitions) {
            if (transition.from == null) {
                throw new UsageException("Transition from is not specified");
            }
            if (transition.to == null) {
                throw new UsageException("Transition to is not specified");
            }
            if (transition.name == null) {
                throw new UsageException("Transition name is not specified");
            }
            dao.addTransition(projectId, transition.from, transition.to, transition.name);
        }
        for (int i = 0; i < project.reports.size(); i++) {
            ProjectReport report = project.reports.get(i);
            if (report.name == null) {
                throw new UsageException("Report name is not specified");
            }
            if (report.json == null && report.simple == null) {
                throw new UsageException("Either simple or json must be specified for report '" + report.name + "'");
            } else if (report.json != null && report.simple != null) {
                throw new UsageException("Both simple and json cannot be specified for report '" + report.name + "'");
            }
            String json = report.json == null ? null : gson.toJson(report.json);
            dao.addReport(projectId, report.name, i + 1, report.simple, json);
        }
        UserDao udao = new UserDao(connection);
        for (Map.Entry<Integer, String> entry : userStates.entrySet()) {
            String stateCode = entry.getValue();
            if (projectStates.contains(stateCode)) {
                int userId = entry.getKey().intValue();
                udao.addUserState(userId, projectId, stateCode);
            }
        }
    }
}
