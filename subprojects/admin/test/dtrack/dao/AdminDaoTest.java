package dtrack.dao;

import dtrack.admin.dao.ProjectDao;
import dtrack.admin.dao.UserDao;
import dtrack.common.dao.DaoTester;

import java.util.Arrays;

public final class AdminDaoTest {

    public static void main(String[] args) throws Throwable {
        DaoTester.testDaos(Arrays.asList(UserDao.class, ProjectDao.class));
    }
}
