package btrack.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public final class DaoTest {

    private static Object generateTestObject(Class<?> cls) {
        if (String.class.isAssignableFrom(cls)) {
            return "A";
        } else if (Character.TYPE.isAssignableFrom(cls) || Character.class.isAssignableFrom(cls)) {
            return 'A';
        } else if (Long.TYPE.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)) {
            return 1L;
        } else if (Integer.TYPE.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls)) {
            return 1;
        } else if (Double.TYPE.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)) {
            return 1.0;
        } else if (Boolean.TYPE.isAssignableFrom(cls) || Boolean.class.isAssignableFrom(cls)) {
            return true;
        } else if (BigDecimal.class.isAssignableFrom(cls)) {
            return BigDecimal.ONE;
        } else if (byte[].class.isAssignableFrom(cls)) {
            return new byte[] {1};
        } else if (Timestamp.class.isAssignableFrom(cls)) {
            return new Timestamp(System.currentTimeMillis());
        } else if (LocalDate.class.isAssignableFrom(cls)) {
            return LocalDate.now();
        } else if (LocalDateTime.class.isAssignableFrom(cls)) {
            return LocalDateTime.now();
        } else if (Enum.class.isAssignableFrom(cls)) {
            return cls.getEnumConstants()[0];
        } else if (cls.isInterface()) {
            return Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[] {cls}, (proxy, method, args) -> generateTestObject(method.getReturnType()));
        } else if (InputStream.class.isAssignableFrom(cls)) {
            return new ByteArrayInputStream(new byte[] {1});
        } else if (OutputStream.class.isAssignableFrom(cls)) {
            return new ByteArrayOutputStream();
        } else {
            throw new IllegalArgumentException("Cannot create parameter of type " + cls);
        }
    }

    private static void invoke(Connection connection, BaseDao dao, Method method) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Object param = generateTestObject(parameterType);
            parameters[i] = param;
        }
        try {
            try {
                method.invoke(dao, parameters);
            } finally {
                connection.rollback();
            }
        } catch (InvocationTargetException itex) {
            System.err.println("Error when running " + method);
            throw itex.getCause();
        }
    }

    private static void invokeAll(Connection connection, BaseDao dao) throws Throwable {
        Method[] declaredMethods = dao.getClass().getDeclaredMethods();
        Arrays.sort(declaredMethods, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        for (Method method : declaredMethods) {
            if (!Modifier.isPublic(method.getModifiers()))
                continue;
            if (Modifier.isStatic(method.getModifiers()))
                continue;
            System.out.println(method.getName());
            invoke(connection, dao, method);
        }
    }

    private static void testDao(Connection connection, Class<? extends BaseDao> cls) throws Throwable {
        Constructor<?>[] constructors = cls.getConstructors();
        Constructor<?> constructor = constructors[0];
        Object[] args = new Object[constructor.getParameterCount()];
        args[0] = connection;
        BaseDao dao = (BaseDao) constructor.newInstance(args);
        invokeAll(connection, dao);
    }

    public static void testDaos(List<Class<? extends BaseDao>> classes) throws Throwable {
        BaseDao.testing = true;
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5431/", "btrack", "btrack")) {
            connection.setAutoCommit(false);
            for (Class<? extends BaseDao> cls : classes) {
                testDao(connection, cls);
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        testDaos(Arrays.asList(BugViewDao.class, BugEditDao.class));
    }
}
