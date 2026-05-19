package lk.ijse.theserenitymentalhealththerapycenter.dao;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import org.hibernate.Session;

public abstract class BaseDAOImpl {
    protected Session getSession() {
        return FactoryConfiguration.getInstance().getSession();
    }
}
