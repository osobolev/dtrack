package btrack.dao;

import btrack.ProjectItem;

public interface LinkFactory {

    String getItemUrl(ProjectItem item, int num, String page);
}
