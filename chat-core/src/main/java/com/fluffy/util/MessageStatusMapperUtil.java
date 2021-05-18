package com.fluffy.util;

import com.fluffy.messaging.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Допоміжний клас, що дозволяє співставляти рядку статус користувача та
 * навпаки.
 * @author Сивоконь Вадим
 */
public final class MessageStatusMapperUtil {
    /**
     * Зберігає рядки для перетворення їх у статуси.
     */
    private static final Map<String, Message.Status> stringMapper;

    /**
     * Зберігає статуси для перетворення їх у рядки.
     */
    private static final Map<Message.Status, String> statusMapper;

    private MessageStatusMapperUtil() { }

    static {
        stringMapper = new HashMap<>();
        statusMapper = new HashMap<>();

        for (Message.Status status : Message.Status.values()) {
            stringMapper.put(status.getGUIString(), status);
            statusMapper.put(status, status.getGUIString());
        }
    }

    /**
     * Повертає графічне представлення у вигляді рядку за вказаним об'єктом.
     * @param status статус користувача
     * @return статус у вигляді рядку
     */
    public static String mapToGUIString(final Message.Status status) {
        return statusMapper.get(status);
    }

    /**
     * Повертає статус у вигляді об'єкту за його рядковим представленням.
     * @param guiString рядкове представлення статусу користувача
     * @return статус користувача
     */
    public static Message.Status mapToStatus(final String guiString) {
        return stringMapper.get(guiString);
    }
}
