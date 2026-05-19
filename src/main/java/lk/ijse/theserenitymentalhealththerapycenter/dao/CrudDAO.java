package lk.ijse.theserenitymentalhealththerapycenter.dao;

import java.util.List;

public interface CrudDAO<T> extends SuperDAO {
    boolean save(T entity) throws Exception;
    boolean update(T entity) throws Exception;
    boolean delete(Object id) throws Exception;
    T findById(Object id) throws Exception;
    List<T> findAll() throws Exception;
}
