package com.fluffy.server.services;

import com.fluffy.callbacks.OnConnectionsCountChangedCallback;
import com.fluffy.callbacks.OnMessageReceivedCallback;
import com.fluffy.callbacks.OnMessageSendCallback;
import com.fluffy.callbacks.OnMessageSendFailCallback;
import com.fluffy.messaging.Message;
import com.fluffy.server.exceptions.ServerShutdownException;
import com.fluffy.server.exceptions.ServerStartupException;
import com.fluffy.server.models.Server;

import java.io.IOException;

/**
 * Клас сервісу для роботи із сервером.
 * @author Сивоконь Вадим
 */
public class ServerService {
    /**
     * Поточний об'єкт сервера.
     */
    private Server server;

    /**
     * Обробник успішного надіслання повідомлення.
     */
    private OnMessageSendCallback onMessageSendCallback;

    /**
     * Обробник невдалого надіслання повідомлення.
     */
    private OnMessageSendFailCallback onMessageSendFailCallback;

    /**
     * Обробник отримання звичайного повідомлення.
     */
    private OnMessageReceivedCallback onMessageReceivedCallback;

    /**
     * Обробник зміни кількості користувачів.
     */
    private OnConnectionsCountChangedCallback onConnectionsCountChangedCallback;

    /**
     * Конструктор об'єкта сервісу.
     */
    public ServerService() {
    }

    // Обробники для виконання додаткової логіки на рівні сервісу

    private void onMessageSend(final Message message) {
        onMessageSendCallback.onMessageSend(message);
    }

    private void onMessageSendFail(final Message message) {
        onMessageSendFailCallback.onMessageSendFail(message);
    }

    private void onMessageReceived(final Message message) {
    }

    private void onConnectionsCountChanged(final int newCount) {
    }

    /**
     * Ініціалізує функції зворотного виклику.
     * @param onMessageSendCallback обробник успішного надіслання повідомлення
     * @param onMessageSendFailCallback обробник невдалого надіслання повідомлення
     * @param onMessageReceivedCallback обробник отримання звичайного повідомлення
     * @param onConnectionsCountChanged обробник зміни кількості користувачів
     */
    public void initCallbacks(final OnMessageSendCallback onMessageSendCallback,
                              final OnMessageSendFailCallback onMessageSendFailCallback,
                              final OnMessageReceivedCallback onMessageReceivedCallback,
                              final OnConnectionsCountChangedCallback onConnectionsCountChanged) {
        this.onMessageSendCallback = onMessageSendCallback;
        this.onMessageSendFailCallback = onMessageSendFailCallback;
        this.onMessageReceivedCallback = onMessageReceivedCallback;
        this.onConnectionsCountChangedCallback = onConnectionsCountChanged;
    }

    /**
     * Запускає новий сервер на вказаному порті.
     * @param port порт
     * @throws ServerStartupException якщо не вдалося запустити сервер
     */
    public void startupServer(final int port)
            throws ServerStartupException {
        if (server == null) {
            server = new Server(port);
            try {
                server.startup((message) -> {
                    ServerService.this.onMessageReceived(message);
                    onMessageReceivedCallback.onMessageReceived(message);
                }, (newCount) -> {
                    ServerService.this.onConnectionsCountChanged(newCount);
                    onConnectionsCountChangedCallback.onConnectionsCountChanged(newCount);
                });
            } catch (IOException e) {
                server = null;
                throw new ServerStartupException("Не вдалося запустити сервер на порті " + port + ": " + e.getMessage(), e);
            }
        } else {
            throw new ServerStartupException("Не вдалося запустити сервер: він уже запущений");
        }
    }

    /**
     * Зупиняє поточний сервер.
     * @throws ServerShutdownException якщо не вдалося зупинити сервер
     */
    public void shutdownServer() throws ServerShutdownException {
        if (isServerActive()) {
            try {
                Message message = new Message();
                message.setType(Message.Type.NOTIFICATION);
                message.setStatus(Message.NotificationStatus.FORCE_CLOSE);
                server.sendMessage(message);

                server.shutdown();
                server = null;
            } catch (IOException e) {
                throw new ServerShutdownException("Не вдалося зупинити сервер: " + e.getMessage(), e);
            }
        } else {
            throw new ServerShutdownException("Не вдалося зупинити сервер: запущеного серверу не було");
        }
    }

    /**
     * Відключає клієнта від сервера за іменем.
     * @param name ім'я
     */
    public void disconnectClientByName(final String name) {
        server.disconnectClientByName(name);
    }

    /**
     * Надсилає повідомлення клієнтам.
     * @param message повідомлення
     */
    public void sendMessage(final Message message) {
        if (isServerActive()) {
            server.sendMessage(message);
            onMessageSend(message);
        } else {
            onMessageSendFail(message);
        }
    }

    /**
     * Повертає стан активності поточного сервера.
     * @return стан активності
     */
    public boolean isServerActive() {
        return (server != null && server.isActive());
    }

    /**
     * Повертає інформацію про поточний сервер.
     * @return інформація про поточний сервер
     */
    public String getServerInfo() {
        if (isServerActive()) {
            return "Запущений сервер: " + server.getIP() + ":" + server.getPort();
        }
        return "";
    }

    /**
     * Повертає інформацію про клієнтів.
     * @return інформація про клієнтів
     */
    public String getClientsInfo() {
        if (isServerActive()) {
            return "Кількість клієнтів: " + server.getClientsCount();
        }
        return "";
    }

    /**
     * Повертає IP сервера.
     * @return IP сервера
     */
    public String getIP() {
        return server.getIP();
    }
}
