package lk.ijse.theserenitymentalhealththerapycenter.dao;

import java.util.List;

public interface CrudDAO<T, ID> extends SuperDAO {
    boolean save(T entity) throws Exception;
    boolean update(T entity) throws Exception;
    boolean delete(ID id) throws Exception;
    T findById(ID id) throws Exception;
    List<T> findAll() throws Exception;
}
