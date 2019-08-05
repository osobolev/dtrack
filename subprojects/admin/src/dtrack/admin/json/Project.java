package dtrack.admin.json;

import java.util.List;

public final class Project {

    public final List<ProjectState> states;
    public final List<ProjectPriority> priorities;
    public final List<ProjectTransition> transitions;
    public final List<ProjectReport> reports;

    public Project(List<ProjectState> states, List<ProjectPriority> priorities, List<ProjectTransition> transitions, List<ProjectReport> reports) {
        this.states = states;
        this.priorities = priorities;
        this.transitions = transitions;
        this.reports = reports;
    }
}
