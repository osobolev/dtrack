package btrack.web.actions;

import btrack.web.dao.BugEditDao;

import java.sql.SQLException;
import java.util.Map;

final class BugData {

    final String title;
    final String safeHtml;
    final String priorityCode;

    private BugData(String title, String safeHtml, String priorityCode) {
        this.title = title;
        this.safeHtml = safeHtml;
        this.priorityCode = priorityCode;
    }

    static BugData create(BugEditDao dao, ProjectInfo request, Map<String, String> parameters) throws ValidationException, SQLException {
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
        if (!dao.validatePriority(request.projectId, priority)) {
            throw new ValidationException("Wrong priority " + priority + " for project " + request.projectName);
        }
        return new BugData(title, safeHtml, priority);
    }
}
