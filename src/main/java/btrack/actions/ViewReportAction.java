package btrack.actions;

import btrack.dao.BugBean;
import btrack.dao.BugViewDao;
import btrack.dao.PriorityBean;
import btrack.dao.ReportBean;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ViewReportAction extends Action {

    private static final String USER_FIELD = "user";

    private final ReportBean report;
    private final ProjectInfo request;

    public ViewReportAction(ReportBean report, ProjectInfo request) {
        this.report = report;
        this.request = request;
    }

    private static boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    private static final class ParsedWhere {

        final String where;
        final List<String> fields;

        ParsedWhere(String where, List<String> fields) {
            this.where = where;
            this.fields = fields;
        }
    }

    private static ParsedWhere parseWhere(String where, boolean simple) {
        int i = 0;
        char quote = 0;
        StringBuilder buf = new StringBuilder();
        List<String> fields = new ArrayList<>();
        while (i < where.length()) {
            char ch = where.charAt(i);
            if (quote != 0) {
                if (ch == quote) {
                    quote = 0;
                    if (simple) {
                        ch = '\'';
                    }
                }
            } else {
                if (ch == '\'' || ch == '"') {
                    quote = ch;
                    if (simple) {
                        ch = '\'';
                    }
                } else if (ch == '$') {
                    int j = i + 1;
                    while (j < where.length()) {
                        char a = where.charAt(j);
                        if (!isAlpha(a))
                            break;
                        j++;
                    }
                    String field = where.substring(i + 1, j);
                    if (USER_FIELD.equals(field)) {
                        buf.append("?");
                        fields.add(field);
                        i = j;
                        continue;
                    }
                }
            }
            buf.append(ch);
            i++;
        }
        return new ParsedWhere(buf.toString(), fields);
    }

    private List<BugBean> performQuery(BugViewDao dao, boolean simple, String where, String orderBy) throws SQLException {
        ParsedWhere parsedWhere = parseWhere(where, simple);
        return dao.listBugs(
            request, false,
            "b.project_id = ? and (" + parsedWhere.where + ")",
            "order by " + (orderBy == null ? "visible_id" : orderBy),
            stmt -> {
                int index = 1;
                stmt.setInt(index++, request.projectId);
                for (String field : parsedWhere.fields) {
                    int n = index++;
                    switch (field) {
                    case USER_FIELD:
                        stmt.setInt(n, request.getUserId());
                        break;
                    }
                }
            }
        );
    }

    private void loadGroup(BugViewDao dao, Object value, List<List<BugBean>> groups) throws ValidationException, SQLException {
        List<BugBean> bugs;
        if (value instanceof String) {
            String where = (String) value;
            bugs = performQuery(dao, false, where, null);
        } else if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            String where = object.optString("where");
            if (where != null) {
                String orderBy = object.optString("orderBy");
                bugs = performQuery(dao, false, where, orderBy);
            } else {
                JSONArray subGroups = object.optJSONArray("groups");
                if (subGroups != null) {
                    for (Object subGroup : subGroups) {
                        loadGroup(dao, subGroup, groups);
                    }
                    return;
                } else {
                    throw new ValidationException("Wrong query in report " + report.id);
                }
            }
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            for (Object item : array) {
                loadGroup(dao, item, groups);
            }
            return;
        } else {
            throw new ValidationException("Wrong query in report " + report.id);
        }
        groups.add(bugs);
    }

    @Override
    public void get(Context ctx, HttpServletResponse resp) throws Exception {
        BugViewDao dao = new BugViewDao(ctx.connection);
        List<List<BugBean>> groups = new ArrayList<>();
        if (report.simpleQuery != null) {
            groups.add(performQuery(dao, true, report.simpleQuery, null));
        } else {
            JSONTokener tok = new JSONTokener(report.jsonQuery);
            loadGroup(dao, tok.nextValue(), groups);
        }
        List<PriorityBean> priorities = dao.listPriorities(request.projectId, null);
        Map<String, Object> params = new HashMap<>();
        request.putTo(params);
        params.put("report", report);
        params.put("bugGroups", groups);
        params.put("priorities", priorities);
        TemplateUtil.process("buglist.ftl", params, resp.getWriter());
    }
}
