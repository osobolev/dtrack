package btrack.dao;

import btrack.common.dao.DaoTester;
import btrack.web.dao.BugEditDao;
import btrack.web.dao.BugViewDao;
import btrack.web.dao.ReportDao;

import java.util.Arrays;

public final class WebDaoTest {

    public static void main(String[] args) throws Throwable {
        DaoTester.testDaos(Arrays.asList(BugViewDao.class, BugEditDao.class, ReportDao.class));
    }
}
