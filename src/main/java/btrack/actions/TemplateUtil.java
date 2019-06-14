package btrack.actions;

import freemarker.cache.FileTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

final class TemplateUtil {

    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_28);

    static {
        try {
            CONFIGURATION.setTemplateLoader(new FileTemplateLoader(new File(".")));
        } catch (IOException ex) {
            ex.printStackTrace();
            // todo: log
        }
        CONFIGURATION.setOutputFormat(HTMLOutputFormat.INSTANCE);
        CONFIGURATION.setDefaultEncoding("UTF-8");
    }

    static void process(String name, Map<String, Object> params, Writer w) throws IOException, TemplateException {
        Template template = CONFIGURATION.getTemplate("src/main/resources/" + name);
        template.process(params, w);
    }

    static void process(String name, Map<String, Object> params, HttpServletResponse resp) throws IOException, ServletException, TemplateException {
        resp.setCharacterEncoding("UTF-8");
        process(name, params, resp.getWriter());
    }
}