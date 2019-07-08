package btrack.dao;

import btrack.ProjectItem;

public interface LinkFactory extends DateFormatter {

    String getItemUrl(ProjectItem item, int num, String page);
}
