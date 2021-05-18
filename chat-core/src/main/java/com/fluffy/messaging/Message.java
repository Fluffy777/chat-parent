package com.fluffy.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Клас повідомлення, об'єктами яких обмінюються клієнт та сервер. Протокол
 * повідомлення:
 * type: TEXT або NOTIFICATION
 * name: ім'я користувача (type = TEXT)
 * content: текст повідомлення (type = TEXT)
 * ip: IP клієнта (type = TEXT)
 * dateTime: час надіслання (type = TEXT)
 * status: значення зі Status (type = TEXT) або значення із NotificationStatus
 * (type = NOTIFICATION)
 * @author Сивоконь Вадим
 */
public class Message implements Serializable {
    /**
     * Тип повідомлення.
     */
    private Type type;

    /**
     * Ім'я користувача.
     */
    private String name;

    /**
     * Текстове повідомлення.
     */
    private String content;

    /**
     * IP адреса клієнта.
     */
    private String ip;

    /**
     * Час надіслання повідомлення.
     */
    private LocalDateTime dateTime;

    /**
     * Містить інформацію про статус (якщо надсилається текстове повідомлення -
     * статус користувача, якщо службове - статус клієнта).
     */
    private Object status;

    /**
     * Конструктор об'єкта повідомлення.
     */
    public Message() {
    }

    /**
     * Можливі типи повідомлень.
     */
    public enum Type {
        /**
         * Звичайний текст.
         */
        TEXT,

        /**
         * Службове повідомлення - сповіщення.
         */
        NOTIFICATION,
    }

    /**
     * Можливі статуси користувача.
     */
    public enum Status {
        /**
         * Праця.
         */
        WORKING,

        /**
         * Сон.
         */
        SLEEPING,

        /**
         * Харчування.
         */
        EATING,

        /**
         * Якщо статус не встановлений.
         */
        NONE;

        /**
         * Повертає для статусу об'єкта його графічне представлення у вигляді
         * рядку.
         * @return рядкове представлення статусу
         */
        public String getGUIString() {
            switch (this) {
                case WORKING:
                    return "Працюю";
                case SLEEPING:
                    return "Сплю";
                case EATING:
                    return "Їм";
                case NONE:
                default:
                    return "—";
            }
        }
    }

    /**
     * Можливі статуси клієнта.
     */
    public enum NotificationStatus {
        /**
         * Статус повідомлення про завершення роботи клієнта (повідомлення
         * надсилається серверу).
         */
        CLOSING,

        /**
         * Статус повідомлення для перевірки наявності підключень від клієнтів
         * до сервера (повідомлення надсилається клієнту).
         */
        KEEPING_ALIVE,

        /**
         * Статус повідомлення для примусового завершення роботи клієнта
         * (повідомлення надсилається клієнту).
         */
        FORCE_CLOSE,

        /**
         * Статус повідомлення, що несе в собі дані авторизації.
         */
        AUTH,
    }

    /**
     * Повертає тип повідомлення.
     * @return тип повідомлення
     */
    public Type getType() {
        return type;
    }

    /**
     * Встановлює тип повідомлення.
     * @param type тип
     */
    public void setType(final Type type) {
        this.type = type;
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
     * Повертає контент повідомлення.
     * @return контент
     */
    public String getContent() {
        return content;
    }

    /**
     * Встановлює контент повідомлення.
     * @param content контент
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Повертає IP клієнта.
     * @return IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * Встановлює IP клієнта.
     * @param ip IP
     */
    public void setIp(final String ip) {
        this.ip = ip;
    }

    /**
     * Повертає час надіслання повідомлення.
     * @return час надіслання
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Встановлює час надіслання повідомлення.
     * @param dateTime час надіслання
     */
    public void setDateTime(final LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Повертає статус повідомлення.
     * @return статус
     */
    public Object getStatus() {
        return status;
    }

    /**
     * Встановлює статус повідомлення.
     * @param status статус
     */
    public void setStatus(final Object status) {
        this.status = status;
    }
}
