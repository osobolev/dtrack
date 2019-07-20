package btrack.admin.json;

import btrack.admin.dao.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Exporter {

    public static void exportProject(Connection connection, int projectId, PrintWriter pw) throws SQLException, IOException {
        ProjectDao dao = new ProjectDao(connection);
        List<StateDTO> states = dao.listStates(projectId);
        List<ProjectState> projectStates = new ArrayList<>();
        for (StateDTO state : states) {
            projectStates.add(new ProjectState(state.code, state.name, state.isDefault ? true : null));
        }
        List<PriorityDTO> priorities = dao.listPriorities(projectId);
        List<ProjectPriority> projectPriorities = new ArrayList<>();
        for (PriorityDTO priority : priorities) {
            projectPriorities.add(new ProjectPriority(priority.code, priority.name, priority.color, priority.isDefault ? true : null));
        }
        Map<String, List<TransitionDTO>> transitions = dao.listTransitions(projectId);
        List<ProjectTransition> projectTransitions = new ArrayList<>();
        for (Map.Entry<String, List<TransitionDTO>> entry : transitions.entrySet()) {
            String from = entry.getKey();
            for (TransitionDTO t : entry.getValue()) {
                projectTransitions.add(new ProjectTransition(from, t.to, t.name));
            }
        }
        List<ProjectReport> projectReports = new ArrayList<>();
        List<ReportDTO> reports = dao.listReports(projectId, true);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        for (ReportDTO report : reports) {
            Object json = report.json == null ? null : gson.fromJson(report.json, Object.class);
            projectReports.add(new ProjectReport(report.name, report.simple, json));
        }
        Project project = new Project(projectStates, projectPriorities, projectTransitions, projectReports);
        String json = gson.toJson(project);
        pw.write(json);
        pw.flush();
    }
}
