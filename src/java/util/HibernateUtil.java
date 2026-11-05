package util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            // Load the database password from the environment variable
            // before configuring Hibernate. The hibernate.cfg.xml file
            // references the placeholder ${db.password} which is resolved
            // from system properties.
            String dbPassword = System.getenv("DB_PASSWORD");
            if (dbPassword == null || dbPassword.isEmpty()) {
                System.err.println("WARNING: DB_PASSWORD environment variable is not set. Hibernate will use an empty password.");
                System.setProperty("db.password", "");
            } else {
                System.setProperty("db.password", dbPassword);
            }

            Configuration configure = new Configuration();
            configure.configure("hibernate.cfg.xml");
            sessionFactory = configure.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}

