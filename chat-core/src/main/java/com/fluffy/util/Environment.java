package com.fluffy.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Допоміжний клас, що дозволяє зберігати в пам'яті налаштування, прочитані з
 * XML-файлу.
 * @author Сивоконь Вадим
 */
public final class Environment {
    private Environment() { }

    /**
     * Внутрішнє сховище.
     */
    private static final Properties properties = new Properties();

    /**
     * Ініціалізує внутрішнє сховище значеннями з XML-файлу.
     * @param xmlFile файл XML
     * @throws IOException якщо сталася помилка під час читання
     */
    public static void initialize(final InputStream xmlFile) throws IOException {
        properties.loadFromXML(xmlFile);
    }

    /**
     * Повертає значення властивості за її назвою (ключем).
     * @param key назва (ключ) властивості
     * @return значення властивості
     */
    public static String getProperty(final String key) {
        return properties.getProperty(key);
    }
}
