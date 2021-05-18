package com.fluffy.client.services;

import com.fluffy.callbacks.OnForceCloseCallback;
import com.fluffy.callbacks.OnMessageReceivedCallback;
import com.fluffy.callbacks.OnMessageSendCallback;
import com.fluffy.callbacks.OnMessageSendFailCallback;
import com.fluffy.client.exceptions.ClientConnectionException;
import com.fluffy.client.exceptions.ClientDisconnectionException;
import com.fluffy.client.models.Client;
import com.fluffy.messaging.Message;

import java.io.IOException;

/**
 * Сервіс для роботи із моделлю клієнта.
 * @author Сивоконь Вадим
 */
public class ClientService {
    /**
     * Модель клієнта.
     */
    private Client client;

    /**
     * Обробник події успішного надіслання повідомлення.
     */
    private OnMessageSendCallback onMessageSendCallback;

    /**
     * Обробник події невдалого надіслання повідомлення.
     */
    private OnMessageSendFailCallback onMessageSendFailCallback;

    /**
     * Обробник події отримання звичайного повідомлення.
     */
    private OnMessageReceivedCallback onMessageReceivedCallback;

    /**
     * Обробник події примусового відключення від сервера.
     */
    private OnForceCloseCallback onForceCloseCallback;

    // Обробники подій

    private void onMessageSend(final Message message) {
    }

    private void onMessageSendFail(final Message message) {
        client = null;
    }

    private void onMessageReceived(final Message message) {
    }

    private void onForceCloseCallback() {
    }

    /**
     * Конструктор об'єкта сервісу.
     */
    public ClientService() {
    }

    /**
     * Ініціалізує функції зворотного виклику.
     * @param onMessageSendCallback обробник успішного надіслання повідомлення
     * @param onMessageSendFailCallback обробник невдалого надіслання повідомлення
     * @param onMessageReceivedCallback обробник отримання звичайного повідомлення
     * @param onForceCloseCallback обробник примусового відключення від сервера
     */
    public void initCallbacks(final OnMessageSendCallback onMessageSendCallback,
                              final OnMessageSendFailCallback onMessageSendFailCallback,
                              final OnMessageReceivedCallback onMessageReceivedCallback,
                              final OnForceCloseCallback onForceCloseCallback) {
        this.onMessageSendCallback = onMessageSendCallback;
        this.onMessageSendFailCallback = onMessageSendFailCallback;
        this.onMessageReceivedCallback = onMessageReceivedCallback;
        this.onForceCloseCallback = onForceCloseCallback;
    }

    /**
     * Підключає до сервера.
     * @param host адреса сервера
     * @param port порт сервера
     * @param name ім'я користувача
     * @param password пароль користувача
     * @throws ClientConnectionException якщо не вдалося підключитися до сервера
     */
    public void connect(final String host,
                        final int port,
                        final String name,
                        final String password)
            throws ClientConnectionException {
        if (client == null) {
            client = new Client(host, port, name, password);
            try {
                client.connect((message -> {
                    ClientService.this.onMessageSend(message);
                    onMessageSendCallback.onMessageSend(message);
                }), (message) -> {
                    ClientService.this.onMessageSendFail(message);
                    onMessageSendFailCallback.onMessageSendFail(message);
                }, (message) -> {
                    ClientService.this.onMessageReceived(message);
                    onMessageReceivedCallback.onMessageReceived(message);
                }, () -> {
                    ClientService.this.onForceCloseCallback();
                    onForceCloseCallback.onForceClose();
                });
            } catch (IOException e) {
                client = null;
                throw new ClientConnectionException("Не вдалося під'єднатися до сервера " + host + ":" + port + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Відключає від сервера.
     * @throws ClientDisconnectionException якщо не вдалося підключитися
     */
    public void disconnect() throws ClientDisconnectionException {
        if (isClientActive()) {
            try {
                client.disconnect();
                client = null;
            } catch (IOException e) {
                throw new ClientDisconnectionException("Не вдалося від'єднатися від сервера: " + e.getMessage(), e);
            }
        } else {
            throw new ClientDisconnectionException("Не вдалося від'єднатися від сервера: підключення раніше не існувало");
        }
    }

    /**
     * Повертає стан клієнта.
     * @return стан клієнта
     */
    public boolean isClientActive() {
        return (client != null && client.isActive());
    }

    /**
     * Повертає інформацію про сервер, до якого здійснено підключення. Якщо,
     * воно відсутнє.
     * @return інформація про сервер
     */
    public String getServerInfo() {
        if (isClientActive()) {
            return "Підключений до сервера " + client.getHost() + ":" + client.getPort();
        }
        return "";
    }

    /**
     * Надсилає повідомлення на сервер.
     * @param message об'єкт повідомлення
     */
    public void sendMessage(final Message message) {
        if (isClientActive()) {
            client.sendMessage(message);
            onMessageSend(message);
        } else {
            onMessageSendFail(message);
        }
    }

    /**
     * Повертає IP клієнта.
     * @return IP клієнта
     */
    public String getIP() {
        return client.getIP();
    }
}
