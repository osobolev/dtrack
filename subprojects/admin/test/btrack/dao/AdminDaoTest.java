package btrack.dao;

import btrack.admin.dao.ProjectDao;
import btrack.admin.dao.UserDao;
import btrack.common.dao.DaoTester;

import java.util.Arrays;

public final class AdminDaoTest {

    public static void main(String[] args) throws Throwable {
        DaoTester.testDaos(Arrays.asList(UserDao.class, ProjectDao.class));
    }
}
