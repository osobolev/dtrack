package dtrack.web.data;

public interface LinkFactory extends DateFormatter {

    String getItemUrl(ProjectItem item, int num, String page);
}
