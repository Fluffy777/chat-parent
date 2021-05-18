package com.fluffy.server.services.impls;

import com.fluffy.server.daos.UserDAO;
import com.fluffy.server.exceptions.DBConnectionException;
import com.fluffy.server.exceptions.PersistException;
import com.fluffy.server.models.User;
import com.fluffy.server.services.UserService;
import com.fluffy.util.ApplicationContext;

import java.util.List;

/**
 * Реалізація сервісу для отримання даних про користувачів.
 * @author Сивоконь Вадим
 */
public class UserServiceImpl implements UserService {
    /**
     * DAO для отримання даних про користувачів.
     */
    private final UserDAO userDAO;

    /**
     * Конструктор об'єкта сервісу.
     */
    public UserServiceImpl() {
        userDAO = (UserDAO) ApplicationContext.lookup("userDAO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User create(final User user) throws DBConnectionException, PersistException {
        return userDAO.insert(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User update(final User user, final Integer id) throws DBConnectionException, PersistException {
        return userDAO.update(id, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(final Integer id) throws DBConnectionException, PersistException {
        return userDAO.delete(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User findById(final Integer id) throws DBConnectionException, PersistException {
        return userDAO.getById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User findByName(final String name) throws DBConnectionException, PersistException {
        return userDAO.getByName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findAll() throws DBConnectionException, PersistException {
        return userDAO.getAll();
    }
}
