package lk.ijse.theserenitymentalhealththerapycenter.config;

import lk.ijse.theserenitymentalhealththerapycenter.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.mindrot.jbcrypt.BCrypt;

public class FactoryConfiguration {

    private static FactoryConfiguration factoryConfiguration;
    private final SessionFactory sessionFactory;

    private FactoryConfiguration() {
        System.out.println(getClass().getResource("/ehcache.xml"));
        try {
            Configuration configuration = new Configuration();

            // 1. Load your standard properties file
            java.util.Properties properties = new java.util.Properties();
            var resourceStream = getClass().getResourceAsStream("/hibernate.properties");
            if (resourceStream == null) {
                throw new java.io.FileNotFoundException("Properties file path not found.");
            }
            properties.load(resourceStream);
            configuration.setProperties(properties);


            // 3. Add your annotated entity classes
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Patient.class);
            configuration.addAnnotatedClass(Therapist.class);
            configuration.addAnnotatedClass(TherapyProgram.class);
            configuration.addAnnotatedClass(TherapySession.class);
            configuration.addAnnotatedClass(Payment.class);

            // 4. Build the factory clean
            sessionFactory = configuration.buildSessionFactory();
            seedDefaultAdmin();

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

    private void seedDefaultAdmin() {
        Session session = sessionFactory.openSession();
        org.hibernate.Transaction transaction = null;
        try {
            // Check if there are any records in the users table
            Long userCount = (Long) session.createQuery("SELECT COUNT(u) FROM User u").uniqueResult();

            if (userCount == 0) {
                transaction = session.beginTransaction();

                // Assuming your User entity uses fields like username, password, fullName, and role
                User admin = new User();
                admin.setUsername("admin");

                // NOTE: If you use BCrypt encryption util, encrypt it: PasswordUtil.hashPassword("admin123")
                admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));

                admin.setFullName("S.A.L.U. Salwathura");
                admin.setRole(lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole.ADMIN);

                session.persist(admin);
                transaction.commit();
                System.out.println(">> Database Seeded: Default account created! [User: admin | Pass: admin123]");
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Database seeding failed: " + e.getMessage());
        } finally {
            session.close();
        }
    }
}