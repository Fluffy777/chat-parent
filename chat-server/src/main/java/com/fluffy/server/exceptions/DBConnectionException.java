package com.fluffy.server.exceptions;

/**
 * Клас винятку, що виникає в разі невдалої спроби отримати з'єднання із базою
 * даних.
 * @author Сивоконь Вадим
 */
public class DBConnectionException extends Exception {
    /**
     * Створює об'єкт винятку.
     */
    public DBConnectionException() {
    }

    /**
     * Створює об'єкт винятку із можливістю вказання текстового повідомлення.
     * @param message текстове повідомлення
     */
    public DBConnectionException(final String message) {
        super(message);
    }

    /**
     * Створює об'єкт винятку із можливістю збереження текстового повідомлення
     * та більш конкретного винятку (його обгортання).
     * @param message текстове повідомлення
     * @param cause більш точна причина винятку
     */
    public DBConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
