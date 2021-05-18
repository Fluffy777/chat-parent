package com.fluffy.server.daos;

import com.fluffy.server.exceptions.DBConnectionException;
import com.fluffy.server.exceptions.PersistException;
import com.fluffy.server.models.User;

import java.util.List;

/**
 * Інтерфейс DAO для отримання даних про користувачів.
 * @author Сивоконь Вадим
 */
public interface UserDAO {
    /**
     * Збереження моделі користувача в базі даних.
     * @param user модель користувача
     * @return модель користувача, збережена в базі даних
     * @throws DBConnectionException якщо сталася помилка з'єднання
     * @throws PersistException якщо сталася помилка під час створення запису
     */
    User insert(User user) throws DBConnectionException, PersistException;

    /**
     * Оновлює моделі користувача в базі даних.
     * @param user модель користувача
     * @param id ID моделі, що оновлюється
     * @return оновлена в базі даних модель користувача
     * @throws DBConnectionException якщо сталася помилка з'єднання
     * @throws PersistException якщо сталася помилка під час оновлення запису
     */
    User update(Integer id, User user) throws DBConnectionException, PersistException;

    /**
     * Видаляє модель користувача із бази даних.
     * @param id ID моделі користувача
     * @return чи вдалося виконати видалення
     * @throws DBConnectionException якщо сталася помилка з'єднання
     * @throws PersistException якщо сталася помилка під час видалення запису
     */
    boolean delete(Integer id) throws DBConnectionException, PersistException;

    /**
     * Повертає модель користувача за її ID із бази даних.
     * @param id ID моделі користувача
     * @return модель користувача
     * @throws DBConnectionException якщо сталася помилка з'єднання
     * @throws PersistException якщо сталася помилка під час виконання запиту
     */
    User getById(Integer id) throws DBConnectionException, PersistException;

    /**
     * Повертає модель користувача за його іменем із бази даних.
     * @param name ім'я користувача
     * @return модель користувача
     * @throws DBConnectionException якщо сталася помилка з'єднання
     * @throws PersistException якщо сталася помилка під час виконання запиту
     */
    User getByName(String name) throws DBConnectionException, PersistException;

    /**
     * Повертає весь список моделей користувачів із бази даних.
     * @return список моделей користувачів
     * @throws DBConnectionException якщо сталася помилка з'єднання
     * @throws PersistException якщо сталася помилка під час виконання запиту
     */
    List<User> getAll() throws DBConnectionException, PersistException;
}
