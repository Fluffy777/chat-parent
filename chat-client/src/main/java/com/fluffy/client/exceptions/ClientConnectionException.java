package com.fluffy.client.exceptions;

/**
 * Клас винятку, що виникає в результаті невдалого підключення до сервера.
 * @author Сивоконь Вадим
 */
public class ClientConnectionException extends Exception {
    /**
     * Створює об'єкт винятку.
     */
    public ClientConnectionException() {
    }

    /**
     * Створює об'єкт винятку із можливістю вказання текстового повідомлення.
     * @param message текстове повідомлення
     */
    public ClientConnectionException(final String message) {
        super(message);
    }

    /**
     * Створює об'єкт винятку із можливістю збереження текстового повідомлення
     * та більш конкретного винятку (його обгортання).
     * @param message текстове повідомлення
     * @param cause більш точна причина винятку
     */
    public ClientConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
