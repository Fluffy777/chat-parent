package com.fluffy.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Допоміжний клас, що дозволяє організувати роботу з контекстом додатку, який
 * дозволяє полішпити зв'язність компонентів та отримати більш простий доступ
 * до окремих об'єктів, коли це є необхідним.
 * @author Сивоконь Вадим
 */
public final class ApplicationContext {
    private ApplicationContext() { }

    /**
     * Внутрішнє сховище.
     */
    private static final Map<String, Object> context = new HashMap<>();

    /**
     * Реєструє об'єкт під іменем в контексті додатку.
     * @param name ім'я
     * @param object об'кт
     */
    public static void registerObject(final String name, final Object object) {
        context.put(name, object);
    }

    /**
     * Скасовує реєстрацію об'єкта в контексті додатку за іменем.
     * @param name ім'я
     */
    public static void unregisterObject(final String name) {
        context.remove(name);
    }

    /**
     * Повертає об'єкт, збережений під вказаним іменем, у контексті додатку.
     * @param name ім'я
     * @return об'єкт із контексту додатку
     */
    public static Object lookup(final String name) {
        return context.get(name);
    }
}
