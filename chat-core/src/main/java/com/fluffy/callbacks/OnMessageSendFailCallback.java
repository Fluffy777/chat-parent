package com.fluffy.callbacks;

import com.fluffy.messaging.Message;

/**
 * Функціональний інтерфейс для реалізацій функцій зворотного виклику на подію
 * невдалого надіслання повідомлення.
 * @author Сивоконь Вадим
 */
public interface OnMessageSendFailCallback {
    /**
     * Функція зворотного виклику.
     * @param message невдало надіслане повідомлення
     */
    void onMessageSendFail(Message message);
}
