package dtrack.dao;

import dtrack.common.dao.DaoTester;
import dtrack.web.dao.BugEditDao;
import dtrack.web.dao.BugViewDao;
import dtrack.web.dao.ReportDao;

import java.util.Arrays;

public final class WebDaoTest {

    public static void main(String[] args) throws Throwable {
        DaoTester.testDaos(Arrays.asList(BugViewDao.class, BugEditDao.class, ReportDao.class));
    }
}
