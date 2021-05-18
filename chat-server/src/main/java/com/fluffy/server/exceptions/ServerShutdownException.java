package com.fluffy.server.exceptions;

/**
 * Клас винятку, що виникає в разі невдалої зупинки сервера.
 * @author Сивоконь Вадим
 */
public class ServerShutdownException extends Exception {
    /**
     * Створює об'єкт винятку.
     */
    public ServerShutdownException() {
    }

    /**
     * Створює об'єкт винятку із можливістю вказання текстового повідомлення.
     * @param message текстове повідомлення
     */
    public ServerShutdownException(final String message) {
        super(message);
    }

    /**
     * Створює об'єкт винятку із можливістю збереження текстового повідомлення
     * та більш конкретного винятку (його обгортання).
     * @param message текстове повідомлення
     * @param cause більш точна причина винятку
     */
    public ServerShutdownException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
