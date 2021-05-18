package com.fluffy.controllers;

import com.fluffy.util.ApplicationContext;

public abstract class AbstractStageController {
    /**
     * ІМ'я контролера для збереження в контексті.
     */
    protected final String controllerName;

    /**
     * Остання відповідь контролеру.
     */
    protected Object lastResponse;

    /**
     * Конструктор об'єкта контролера.
     */
    protected AbstractStageController() {
        String className = getClass().getSimpleName();
        char[] chars = className.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        controllerName = new String(chars);

        ApplicationContext.registerObject(controllerName, this);
    }

    /**
     * Повертає ім'я контролера.
     * @return ім'я контролера
     */
    public String getControllerName() {
        return controllerName;
    }

    /**
     * Встановлює відповідь, яку повинен обробити контролер у майбутньому.
     * @param response відповідь для контролера
     */
    public void setLastResponse(final Object response) {
        lastResponse = response;
    }
}
