package com.fluffy.util;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Допоміжний клас списку LinkedList. Основна його відмінність від звичайного -
 * автоматичне видалення з нього попередніх елементів на випадок перевищення
 * максимальної місткості.
 * @param <E> тип елемента списку
 */
public class LimitedLinkedList<E> extends LinkedList<E> {
    /**
     * Максимальна місткість списку.
     */
    private final int maxSize;

    /**
     * Конструктор об'єкта списку з обмеженою місткістю.
     * @param maxSize максимальна місткість
     */
    public LimitedLinkedList(final int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
    }

    private void ensureMaxSize() {
        int exrtaElementsCount = this.size() - maxSize;
        for (int i = 0; i < exrtaElementsCount; ++i) {
            this.poll();
        }
    }

    /**
     * Додає елемент в початок списку.
     * @param e елемент
     * @return чи вдалося додати елемент
     */
    @Override
    public boolean add(final E e) {
        boolean result = super.add(e);
        ensureMaxSize();
        return result;
    }

    /**
     * Додає елемент в кінець списку.
     * @param e елемент
     * @return чи вдалося додати елемент
     */
    @Override
    public boolean offer(final E e) {
        boolean result = super.offer(e);
        ensureMaxSize();
        return result;
    }

    /**
     * Додає всі елементи, що містить колекція, до списку.
     * @param c колекція елементів
     * @return чи вдалося змінити поточний список
     */
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        boolean result = super.addAll(c);
        ensureMaxSize();
        return result;
    }
}
