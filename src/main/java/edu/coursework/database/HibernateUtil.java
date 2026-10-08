package edu.coursework.database;

import edu.coursework.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

// Единственный SessionFactory приложения — простейший Singleton.
public final class HibernateUtil {
    private static final HibernateUtil INSTANCE = new HibernateUtil();
    private final SessionFactory sessionFactory;

    private HibernateUtil() {
        Configuration c = new Configuration();
        c.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        c.setProperty("hibernate.connection.url", env("DB_URL", "jdbc:mysql://localhost:3306/facultatives?useUnicode=true&characterEncoding=UTF-8"));
        c.setProperty("hibernate.connection.username", env("DB_USER", "root"));
        c.setProperty("hibernate.connection.password", env("DB_PASSWORD", ""));
        c.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        c.setProperty("hibernate.hbm2ddl.auto", "validate"); // Таблицы создаёт SQL, не Hibernate.
        c.setProperty("hibernate.show_sql", "false");
        c.addAnnotatedClass(Student.class);
        c.addAnnotatedClass(Department.class);
        c.addAnnotatedClass(Subject.class);
        c.addAnnotatedClass(Elective.class);
        c.addAnnotatedClass(Semester.class);
        c.addAnnotatedClass(Offering.class);
        c.addAnnotatedClass(Enrollment.class);
        c.addAnnotatedClass(User.class);
        sessionFactory = c.buildSessionFactory();
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null ? fallback : value;
    }

    public static SessionFactory factory() {
        return INSTANCE.sessionFactory;
    }

    public static void shutdown() {
        INSTANCE.sessionFactory.close();
    }
}
