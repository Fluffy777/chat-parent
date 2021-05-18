package com.fluffy.server.models;

/**
 * Клас моделі користувача.
 * @author Сивоконь Вадим
 */
public class User {
    /**
     * ID користувача.
     */
    private Integer id;

    /**
     * Ім'я користувача.
     */
    private String name;

    /**
     * Пароль користувача.
     */
    private String password;

    /**
     * Конструктор об'єкта моделі користувача.
     */
    public User() {
    }

    /**
     * Повертає ID користувача.
     * @return ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Встановлює ID користувача.
     * @param id ID
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Повертає ім'я користувача.
     * @return ім'я
     */
    public String getName() {
        return name;
    }

    /**
     * Встановлює ім'я користувача.
     * @param name ім'я
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Повертає пароль користувача.
     * @return пароль
     */
    public String getPassword() {
        return password;
    }

    /**
     * Встановлює пароль користувача.
     * @param password пароль
     */
    public void setPassword(final String password) {
        this.password = password;
    }
}
