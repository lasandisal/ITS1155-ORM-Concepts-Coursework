package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.BaseDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.UserDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import org.hibernate.query.Query;

import java.util.List;

public class UserDAOImpl extends BaseDAOImpl implements UserDAO {
    @Override
    public boolean save(User entity) throws Exception {
        getSession().persist(entity);
        return true;
    }

    @Override
    public boolean update(User entity) throws Exception {
        getSession().merge(entity);
        return true;
    }

    @Override
    public boolean delete(Long id) throws Exception {
        User user = findById(id);
        if (user != null) {
            user.setStatus(CommonStatus.INACTIVE);
            getSession().merge(user);
            return true;
        }
        return false;
    }

    @Override
    public User findById(Long id) throws Exception {
        return getSession().get(User.class, id);
    }


    @Override
    public List<User> findAll() throws Exception {
        return getSession().createQuery("FROM User", User.class).list();
    }

    @Override
    public User findByUsername(String username) throws Exception {
        Query<User> query = getSession().createQuery("FROM User WHERE username = :uname AND status = 'ACTIVE'", User.class);
        query.setParameter("uname", username);
        return query.uniqueResult();
    }

    @Override
    public boolean existsByUsername(String username) throws Exception {
        Query<Long> query = getSession().createQuery("SELECT count(u.id) FROM User u WHERE u.username = :uname", Long.class);
        query.setParameter("uname", username);
        return query.uniqueResult() > 0;
    }
}
