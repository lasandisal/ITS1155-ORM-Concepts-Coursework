package lk.ijse.theserenitymentalhealththerapycenter.config;

import lk.ijse.theserenitymentalhealththerapycenter.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class FactoryConfiguration {

    private static FactoryConfiguration factoryConfiguration;
    private final SessionFactory sessionFactory;

    private FactoryConfiguration() {
        try {
            Configuration configuration = new Configuration();

            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Patient.class);
            configuration.addAnnotatedClass(Therapist.class);
            configuration.addAnnotatedClass(TherapyProgram.class);
            configuration.addAnnotatedClass(TherapySession.class);
            configuration.addAnnotatedClass(Payment.class);

            sessionFactory = configuration.buildSessionFactory();

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
        return sessionFactory.openSession();
    }
}