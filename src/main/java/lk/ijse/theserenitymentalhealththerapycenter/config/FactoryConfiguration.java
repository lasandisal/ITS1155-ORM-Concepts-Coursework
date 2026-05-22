package lk.ijse.theserenitymentalhealththerapycenter.config;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.mindrot.jbcrypt.BCrypt;

import java.io.FileNotFoundException;
import java.util.Properties;

public class FactoryConfiguration {

    private static FactoryConfiguration factoryConfiguration;
    private final SessionFactory sessionFactory;

    private FactoryConfiguration() {
        System.out.println(getClass().getResource("/ehcache.xml"));
        try {
            Configuration configuration = new Configuration();

            Properties properties = new Properties();
            var resourceStream = getClass().getResourceAsStream("/hibernate.properties");
            if (resourceStream == null) {
                throw new FileNotFoundException("Properties file path not found.");
            }
            properties.load(resourceStream);
            configuration.setProperties(properties);

            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Patient.class);
            configuration.addAnnotatedClass(Therapist.class);
            configuration.addAnnotatedClass(TherapyProgram.class);
            configuration.addAnnotatedClass(TherapySession.class);
            configuration.addAnnotatedClass(SessionAttendance.class);
            configuration.addAnnotatedClass(Payment.class);

            sessionFactory = configuration.buildSessionFactory();
            seedInitialUsers();

        } catch (Exception e) {
            System.err.println("Initial SessionFactory creation failed! " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static FactoryConfiguration getInstance() {
        if (factoryConfiguration == null) {
            factoryConfiguration = new FactoryConfiguration();
        }
        return factoryConfiguration;
    }


    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    private void seedInitialUsers() {
        Session session = sessionFactory.openSession();
        org.hibernate.Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            User admin = session.get(User.class, 1L);

            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
                admin.setFullName("LasandiSal");
                admin.setEmail("admin@theserenity.com");
                admin.setRole(UserRole.ADMIN);
                admin.setStatus(CommonStatus.ACTIVE);

                session.persist(admin);
                System.out.println(">> Database Seeded: Default Admin created at ID 1! [User: admin | Pass: admin123]");
            }

            User receptionist = session.get(User.class, 2L);

            if (receptionist == null) {
                receptionist = new User();
                receptionist.setUsername("receptionist");
                receptionist.setPassword(BCrypt.hashpw("recep123", BCrypt.gensalt()));
                receptionist.setFullName("SelinErl");
                receptionist.setEmail("reception@theserenity.com");
                receptionist.setRole(UserRole.RECEPTIONIST);
                receptionist.setStatus(CommonStatus.ACTIVE);

                session.persist(receptionist);
                System.out.println(">> Database Seeded: Default Receptionist created at ID 2! [User: receptionist | Pass: recep123]");
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Database seeding failed: " + e.getMessage());
        } finally {
            session.close();
        }
    }
}