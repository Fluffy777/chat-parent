package com.fluffy.server.models;

import com.fluffy.callbacks.OnMessageReceivedCallback;
import com.fluffy.messaging.Message;
import com.fluffy.server.exceptions.DBConnectionException;
import com.fluffy.server.exceptions.PersistException;
import com.fluffy.server.services.UserService;
import com.fluffy.util.ApplicationContext;
import com.fluffy.util.Environment;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * Клас обробника підключення клієнта до сервера.
 * @author Сивоконь Вадим
 */
public class ClientHandler implements Runnable {
    /**
     * Період читання повідомлень, що надходять.
     */
    private static final int READ_PERIOD = Integer.parseInt(Environment.getProperty("server.client-handler.read-period"));

    /**
     * Сокет клієнта.
     */
    private final Socket clientSocket;

    /**
     * Сервер.
     */
    private final Server server;

    /**
     * Сервіс для отримання даних про користувачів.
     */
    private final UserService userService;

    /**
     * Ім'я користувача.
     */
    private String name;

    /**
     * Потік об'єктів для отримання повідомлень від клієнта.
     */
    private final ObjectInputStream objectInputStream;

    /**
     * Потік об'єктів для надіслання повідомлень клієнту.
     */
    private final ObjectOutputStream objectOutputStream;

    /**
     * Обробник отримання звичайного повідомлення.
     */
    private final OnMessageReceivedCallback onMessageReceivedCallback;

    /**
     * Конструктор обробника підключення до сервера.
     * @param clientSocket сокет клієнта
     * @param server сервер
     * @param onMessageReceivedCallback обробник отримання звичайного повідомлення
     * @throws IOException якщо сталася помилка під час роботи із I/O-потоками
     */
    public ClientHandler(final Socket clientSocket,
                         final Server server,
                         final OnMessageReceivedCallback onMessageReceivedCallback) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;

        this.userService = (UserService) ApplicationContext.lookup("userService");

        // обов'язково: спочатку output, потім - input
        this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

        this.onMessageReceivedCallback = onMessageReceivedCallback;
    }

    /**
     * Синхронізоване надіслання повідомлення клієнту.
     * @param message повідомлення
     * @throws IOException якщо сталася помилка під час надіслання
     */
    public synchronized void sendMessage(final Message message) throws IOException {
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
    }

    /**
     * Закриває з'єднання із клієнтом та видаляє поточний обробник зі списку.
     */
    public void close() {
        try {
            clientSocket.close();
            server.removeClient(this);
        } catch (IOException e) {
            // не вдалося завершити з'єднання
        }
    }

    private void onMessageReceived(final Message message) {
        if (message.getType().equals(Message.Type.NOTIFICATION)) {
            switch ((Message.NotificationStatus) message.getStatus()) {
                case CLOSING:
                    // працює KEEP ALIVE, але надсилання повідомлення прискорить процес
                    close();
                    break;
                case AUTH:
                    String name = message.getName();
                    String password = message.getContent();

                    this.name = name;
                    try {
                        boolean auth = false;
                        User user = userService.findByName(name);
                        if (user != null) {
                            auth = BCrypt.checkpw(password, user.getPassword());
                        }

                        if (!auth) {
                            server.disconnectClientByName(name);
                        } else {
                            // перевірка на максимальну кількість підключень
                            if (server.getClientsCount() > Server.CONNECTIONS_LIMIT) {
                                server.disconnectClientByName(name);
                            } else {
                                // надсилаємо історію повідомлень
                                server.sendLastMessages(this);
                            }
                        }
                    } catch (DBConnectionException | PersistException e) {
                        // не вдалося перевірити - відключаємо
                        server.disconnectClientByName(name);
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (onMessageReceivedCallback != null) {
                // для звичайного повідомлення можна дозволити
                // виконання розсилки та зворотного виклику
                server.sendMessage(message, this);
                onMessageReceivedCallback.onMessageReceived(message);
            }
        }
    }

    /**
     * Виконує основну логіку обробника підключення до сервера в окремому
     * потоці.
     */
    @Override
    public void run() {
        // слухаємо поточного клієнта
        while (!Thread.currentThread().isInterrupted() && server.isActive() && !clientSocket.isClosed()) {
            Message message = null;
            try {
                message = (Message) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                // не вдалося прочитати повідомлення
            }

            if (message != null) {
                onMessageReceived(message);
            }

            try {
                Thread.sleep(READ_PERIOD);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Повертає ім'я користувача.
     * @return ім'я користувача
     */
    public String getName() {
        return name;
    }

    /**
     * Порівнює об'єкти за вмістом.
     * @param o інший об'єкт
     * @return чи є вони рівними за вмістом
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientHandler that = (ClientHandler) o;
        return Objects.equals(clientSocket, that.clientSocket) && Objects.equals(server, that.server) && Objects.equals(objectInputStream, that.objectInputStream) && Objects.equals(objectOutputStream, that.objectOutputStream) && Objects.equals(onMessageReceivedCallback, that.onMessageReceivedCallback);
    }

    /**
     * Повертає хеш-код об'єкта.
     * @return хеш-код
     */
    @Override
    public int hashCode() {
        return Objects.hash(clientSocket, server, objectInputStream, objectOutputStream, onMessageReceivedCallback);
    }
}
