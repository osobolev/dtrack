package dtrack.web.actions;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public final class TemplateUtil {

    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_32);

    public static void init(String dir) throws IOException {
        if (dir == null) {
            CONFIGURATION.setTemplateLoader(new ClassTemplateLoader(TemplateUtil.class, "/"));
        } else {
            CONFIGURATION.setTemplateLoader(new FileTemplateLoader(new File(dir)));
        }
        CONFIGURATION.setOutputFormat(HTMLOutputFormat.INSTANCE);
        CONFIGURATION.setDefaultEncoding("UTF-8");
    }

    static void process(String name, Map<String, Object> params, Writer w) throws IOException, TemplateException {
        Template template = CONFIGURATION.getTemplate(name);
        template.process(params, w);
    }
}
