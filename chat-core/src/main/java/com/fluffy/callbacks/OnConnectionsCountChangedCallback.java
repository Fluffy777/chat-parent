package com.fluffy.callbacks;

/**
 * Функціональний інтерфейс для реалізацій функцій зворотного виклику на подію
 * зміни кількості підключень.
 * @author Сивоконь Вадим
 */
public interface OnConnectionsCountChangedCallback {
    /**
     * Функція зворотного виклику.
     * @param newCount нова кількість підключень
     */
    void onConnectionsCountChanged(int newCount);
}
