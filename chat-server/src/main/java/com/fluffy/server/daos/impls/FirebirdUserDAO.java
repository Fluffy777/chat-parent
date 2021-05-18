package com.fluffy.server.daos.impls;

import com.fluffy.server.daos.UserDAO;
import com.fluffy.server.exceptions.DBConnectionException;
import com.fluffy.server.exceptions.PersistException;
import com.fluffy.server.models.User;
import com.fluffy.server.util.DataSource;
import com.fluffy.util.ApplicationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * Клас DAO, об'єкти якого дозволяють отримувати дані про користувачів.
 * @author Сивоконь Вадим
 */
public class FirebirdUserDAO implements UserDAO {
    /**
     * Запит на створення нового користувача.
     */
    private static final String QUERY_INSERT = "INSERT INTO \"user\" (id, name, password) VALUES (NEXT VALUE FOR \"user_seq\", ?, ?)";

    /**
     * Запит на оновлення користувача.
     */
    private static final String QUERY_UPDATE = "UPDATE \"user\" SET name = ?, password = ? WHERE id = ?";

    /**
     * Запит на видалення користувача.
     */
    private static final String QUERY_DELETE = "DELETE FROM \"user\" WHERE id = ?";

    /**
     * Запит на отримання всіх користувачів.
     */
    private static final String QUERY_GET_ALL = "SELECT * FROM \"user\"";

    /**
     * Запит на отримання користувача за ID.
     */
    private static final String QUERY_GET_BY_ID = QUERY_GET_ALL + " WHERE id = ?";

    /**
     * Запит на отримання користувача за іменем.
     */
    private static final String QUERY_GET_BY_NAME = QUERY_GET_ALL + " WHERE name = ?";

    /**
     * Джерело даних.
     */
    private final DataSource dataSource;

    private User newInstance(final ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        user.setPassword(resultSet.getString("password"));
        return user;
    }

    /**
     * Конструктор DAO.
     */
    public FirebirdUserDAO() {
        dataSource = (DataSource) ApplicationContext.lookup("dataSource");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User insert(final User user) throws DBConnectionException, PersistException {
        try (Connection connection = dataSource.getConnection()) {
            String[] columns = new String[]{"ID", "NAME", "PASSWORD"};
            try (PreparedStatement statement = connection.prepareStatement(QUERY_INSERT, columns)) {
                int seq = 0;
                statement.setString(++seq, user.getName());
                statement.setString(++seq, user.getPassword());

                if (statement.executeUpdate() > 0) {
                    ResultSet resultSet = statement.getGeneratedKeys();
                    resultSet.next();

                    int id = resultSet.getInt(1);
                    user.setId(id);

                    return user;
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new PersistException("Не вдалося виконати запит на додавання інформації про користувача", e);
            }
        } catch (DBConnectionException | SQLException e) {
            throw new DBConnectionException("Помилка з'єднання із базою даних", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User update(final Integer id, final User user) throws DBConnectionException, PersistException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_UPDATE)) {
                int seq = 0;
                statement.setString(++seq, user.getName());
                statement.setString(++seq, user.getPassword());
                statement.setInt(++seq, id);

                if (statement.executeUpdate() > 0) {
                    return user;
                }
            } catch (SQLException e) {
                throw new PersistException("Не вдалося виконати запит на оновлення інформації про користувача", e);
            }
        } catch (DBConnectionException | SQLException e) {
            throw new DBConnectionException("Помилка з'єднання із базою даних", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(final Integer id) throws DBConnectionException, PersistException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_DELETE)) {
                statement.setInt(1, id);

                return statement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new PersistException("Не вдалося виконати запит на видалення інформації про користувача");
            }
        } catch (DBConnectionException | SQLException e) {
            throw new DBConnectionException("Помилка з'єднання із базою даних", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getById(final Integer id) throws DBConnectionException, PersistException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY_GET_BY_ID);
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = newInstance(resultSet);
                    if (resultSet.next()) {
                        throw new PersistException("Існує декілька користувачів, у яких id = " + id);
                    }
                    return user;
                }
                return null;
            } catch (SQLException e) {
                throw new PersistException("Не вдалося виконати запит на отримання даних про користувача, у якого id = " + id, e);
            }
        } catch (DBConnectionException | SQLException e) {
            throw new DBConnectionException("Помилка з'єднання із базою даних", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getByName(final String name) throws DBConnectionException, PersistException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY_GET_BY_NAME);
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = newInstance(resultSet);
                    if (resultSet.next()) {
                        throw new PersistException("Існує декілька користувачів, у яких name = " + name);
                    }
                    return user;
                }
                return null;
            } catch (SQLException e) {
                throw new PersistException("Не вдалося виконати запит на отримання даних про користувача, у якого name = " + name, e);
            }
        } catch (DBConnectionException | SQLException e) {
            throw new DBConnectionException("Помилка з'єднання із базою даних", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getAll() throws DBConnectionException, PersistException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(QUERY_GET_ALL)) {
                List<User> users = new LinkedList<>();
                while (resultSet.next()) {
                    users.add(newInstance(resultSet));
                }
                return users;
            } catch (SQLException e) {
                throw new PersistException("Не вдалося виконати запит на отримання даних про всіх користувачів", e);
            }
        } catch (DBConnectionException | SQLException e) {
            throw new DBConnectionException("Помилка з'єднання із базою даних", e);
        }
    }
}
