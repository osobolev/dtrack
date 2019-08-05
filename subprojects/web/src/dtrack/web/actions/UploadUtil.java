package dtrack.web.actions;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class UploadUtil<T> {

    private static final PolicyFactory IMAGES = new HtmlPolicyBuilder()
        .allowElements("img")
        .allowUrlProtocols("data")
        .allowAttributes("alt", "src", "data-filename", "border", "height", "width").onElements("img")
        .toFactory();

    static final PolicyFactory POLICY = IMAGES
        .and(Sanitizers.FORMATTING)
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.STYLES)
        .and(Sanitizers.LINKS)
        .and(Sanitizers.TABLES);

    interface CreateItem<T> {

        T create(Map<String, String> parameters) throws Exception;
    }

    interface Upload<T> {

        void upload(T result, String fileName, InputStream content) throws Exception;
    }

    private final Map<String, String> parameters = new HashMap<>();
    private final CreateItem<T> create;

    private T result = null;
    private boolean resultCalculated = false;

    UploadUtil(CreateItem<T> create) {
        this.create = create;
    }

    private T getResult() throws Exception {
        if (!resultCalculated) {
            resultCalculated = true;
            result = create.create(parameters);
        }
        return result;
    }

    T post(HttpServletRequest req, Upload<T> upload) throws Exception {
        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
        fileItemFactory.setDefaultCharset("UTF-8");
        ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
        List<FileItem> fileItems = fileUpload.parseRequest(req);
        for (FileItem fileItem : fileItems) {
            if (!fileItem.isFormField()) {
                if (fileItem.getName() == null || fileItem.getName().isEmpty())
                    continue;
                T result = getResult();
                String fileName = new File(fileItem.getName()).getName();
                try (InputStream is = fileItem.getInputStream()) {
                    upload.upload(result, fileName, is);
                }
            } else {
                String fieldName = fileItem.getFieldName();
                if (fieldName == null)
                    continue;
                String value = fileItem.getString();
                parameters.put(fieldName, value);
            }
        }
        return getResult();
    }
}
