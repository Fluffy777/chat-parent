package com.fluffy.callbacks;

import com.fluffy.messaging.Message;

/**
 * Функціональний інтерфейс для реалізацій функцій зворотного виклику на подію
 * отримання звичайного повідомлення.
 * @author Сивоконь Вадим
 */
public interface OnMessageReceivedCallback {
    /**
     * Функція зворотного виклику.
     * @param message отримане повідомлення
     */
    void onMessageReceived(Message message);
}
