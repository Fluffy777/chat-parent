package com.fluffy.callbacks;

import com.fluffy.messaging.Message;

/**
 * Функціональний інтерфейс для реалізацій функцій зворотного виклику на подію
 * успішного надіслання повідомлення.
 * @author Сивоконь Вадим
 */
public interface OnMessageSendCallback {
    /**
     * Функція зворотного виклику.
     * @param message успішно надіслане повідомлення
     */
    void onMessageSend(Message message);
}
