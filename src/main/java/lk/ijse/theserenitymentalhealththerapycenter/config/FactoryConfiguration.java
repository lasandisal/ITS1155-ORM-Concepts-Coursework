package lk.ijse.theserenitymentalhealththerapycenter.config;

import lk.ijse.theserenitymentalhealththerapycenter.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Properties;

public class FactoryConfiguration {

    private static FactoryConfiguration factoryConfiguration;
    private final SessionFactory sessionFactory;

    private FactoryConfiguration() {
        System.out.println(getClass().getResource("/ehcache.xml"));
        try {
            Configuration configuration = new Configuration();

            Properties properties = new java.util.Properties();
            var resourceStream = getClass().getResourceAsStream("/hibernate.properties");
            if (resourceStream == null) {
                throw new java.io.FileNotFoundException("Properties file path not found.");
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

            Long adminCount = (Long) session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = 'admin'"
            ).uniqueResult();

            if (adminCount == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
                admin.setFullName("LasandiSal");
                admin.setEmail("admin@theserenity.com");
                admin.setRole(lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole.ADMIN);


                admin.setStatus(lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus.ACTIVE);

                session.persist(admin);
                System.out.println(">> Database Seeded: Default Admin created! [User: admin | Pass: admin123]");
            }

            Long recepCount = (Long) session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = 'receptionist'"
            ).uniqueResult();

            if (recepCount == 0) {
                User receptionist = new User();
                receptionist.setUsername("receptionist");
                receptionist.setPassword(BCrypt.hashpw("recep123", BCrypt.gensalt()));
                receptionist.setFullName("SelinErl");
                receptionist.setEmail("reception@theserenity.com");
                receptionist.setRole(lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole.RECEPTIONIST);
                receptionist.setStatus(lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus.ACTIVE);

                session.persist(receptionist);
                System.out.println(">> Database Seeded: Default Receptionist created! [User: receptionist | Pass: recep123]");
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