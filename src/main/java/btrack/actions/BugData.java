package btrack.actions;

import btrack.AccessUtil;
import btrack.dao.BugEditDao;

import java.sql.SQLException;
import java.util.Map;

final class BugData {

    final String title;
    final String safeHtml;
    final int priorityId;

    private BugData(String title, String safeHtml, int priorityId) {
        this.title = title;
        this.safeHtml = safeHtml;
        this.priorityId = priorityId;
    }

    static BugData create(BugEditDao dao, int projectId, Map<String, String> parameters) throws ValidationException, SQLException {
        String title = parameters.get("title");
        if (title == null)
            throw new ValidationException("Missing 'title' parameter");
        String untrustedHtml = parameters.get("html");
        if (untrustedHtml == null)
            throw new ValidationException("Missing 'html' parameter");
        String priority = parameters.get("priority");
        if (priority == null)
            throw new ValidationException("Missing 'priority' parameter");
        String safeHtml = UploadUtil.POLICY.sanitize(untrustedHtml);
        int priorityId = AccessUtil.parseInt(priority);
        if (!dao.validatePriority(projectId, priorityId)) {
            throw new ValidationException("Wrong priority " + priorityId + " for project " + projectId);
        }
        return new BugData(title, safeHtml, priorityId);
    }
}
