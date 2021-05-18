package com.fluffy.callbacks;

/**
 * Функціональний інтерфейс для реалізацій функцій зворотного виклику на подію
 * примусового відключення клієнта від сервера.
 * @author Сивоконь Вадим
 */
public interface OnForceCloseCallback {
    /**
     * Функція зворотного виклику.
     */
    void onForceClose();
}
