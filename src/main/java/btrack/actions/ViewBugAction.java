package btrack.actions;

import btrack.dao.BugBean;
import btrack.dao.BugsDao;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class ViewBugAction extends Action {

    private final int bugId;

    public ViewBugAction(int bugId) {
        this.bugId = bugId;
    }

    @Override
    public void get(Context ctx, HttpServletRequest req, Writer out) throws SQLException, ValidationException, IOException, TemplateException {
        BugsDao dao = new BugsDao(ctx.connection);
        BugBean bugData = dao.loadBug(bugId);
        Map<String, Object> params = new HashMap<>();
        params.put("bug", bugData);
        TemplateUtil.process("viewbug.ftl", params, out);
    }
}
