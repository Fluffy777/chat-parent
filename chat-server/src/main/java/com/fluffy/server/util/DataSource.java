package com.fluffy.server.util;

import com.fluffy.server.exceptions.DBConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Допоміжний клас, дозволяє зберігати параметри підключення до бази даних та
 * отримувати його.
 * @author Сивоконь Вадим
 */
public class DataSource {
    /**
     * URL.
     */
    private final String url;

    /**
     * Ім'я користувача.
     */
    private final String username;

    /**
     * Пароль користувача.
     */
    private final String password;

    /**
     * Назва класа драйвера.
     */
    private final String driverClass;

    /**
     * Конструктор об'єкта джерела даних.
     * @param url URL
     * @param username ім'я користувача
     * @param password пароль
     * @param driverClass назва класа драйвера
     * @throws ClassNotFoundException якщо клас драйвера не вдалося знайти
     */
    public DataSource(final String url, final String username, final String password, final String driverClass) throws ClassNotFoundException {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClass = driverClass;

        Class.forName(driverClass);
    }

    /**
     * Повертає з'єднання із базою даних відповідно до встановлених параметрів.
     * @return з'єднання
     * @throws DBConnectionException якщо не вдалося підключитися до бази даних
     */
    public Connection getConnection() throws DBConnectionException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DBConnectionException("Не вдалося отримати з'єднання із базою даних");
        }
    }

    /**
     * Повертає URL.
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Повертає ім'я користувача.
     * @return ім'я користувача
     */
    public String getUsername() {
        return username;
    }

    /**
     * Повертає пароль користувача.
     * @return пароль користувача
     */
    public String getPassword() {
        return password;
    }

    /**
     * Повертає назву класа драйвера.
     * @return назва класа драйвера
     */
    public String getDriverClass() {
        return driverClass;
    }
}
