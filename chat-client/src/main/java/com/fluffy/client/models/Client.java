package com.fluffy.client.models;

import com.fluffy.callbacks.OnForceCloseCallback;
import com.fluffy.callbacks.OnMessageReceivedCallback;
import com.fluffy.callbacks.OnMessageSendCallback;
import com.fluffy.callbacks.OnMessageSendFailCallback;
import com.fluffy.messaging.Message;
import com.fluffy.util.Environment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Клас моделі клієнта.
 * @author Сивоконь Вадим
 */
public class Client implements Runnable {
    /**
     * Період виконання циклу в основному методі {@link #run() run}.
     */
    private static final int LISTENING_PERIOD = Integer.parseInt(Environment.getProperty("client.listening-period"));

    /**
     * Адреса для отримання IP сервера.
     */
    private static final String IP_TEST_HOST = Environment.getProperty("client.ip-test-host");

    /**
     * Порт для отримання IP сервера.
     */
    private static final int IP_TEST_PORT = Integer.parseInt(Environment.getProperty("client.ip-test-port"));

    /**
     * Адреса сервера.
     */
    private final String host;

    /**
     * Порт, на якому запущений сервер.
     */
    private final int port;

    /**
     * Ім'я користувача.
     */
    private final String name;

    /**
     * Пароль користувача.
     */
    private final String password;

    /**
     * Потік для отримання повідомлень від сервера.
     */
    private ObjectInputStream objectInputStream;

    /**
     * Потік для надсилання повідомлень на сервер.
     */
    private ObjectOutputStream objectOutputStream;

    /**
     * Сокет клієнта.
     */
    private Socket clientSocket;

    /**
     * Поточний статус клієнта.
     */
    private boolean active;

    // Обробники подій

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

    private void onMessageSend(final Message message) {
        if (onMessageSendCallback != null && !message.getType().equals(Message.Type.NOTIFICATION)) {
            // звичайне повідомлення - можна рахувати як таке, що можна
            // відобразити
            onMessageSendCallback.onMessageSend(message);
        }
    }

    private void onMessageSendFail(final Message message) {
        if (onMessageSendFailCallback != null && !message.getType().equals(Message.Type.NOTIFICATION)) {
            onMessageSendFailCallback.onMessageSendFail(message);
        }
    }

    private void onMessageReceived(final Message message) {
        Message.Type type = message.getType();
        if (onMessageReceivedCallback != null && !type.equals(Message.Type.NOTIFICATION)) {
            onMessageReceivedCallback.onMessageReceived(message);
        } else if (type.equals(Message.Type.NOTIFICATION) && message.getStatus().equals(Message.NotificationStatus.FORCE_CLOSE)) {
            onForceClose();
        }
    }

    private void onForceClose() {
        onForceCloseCallback.onForceClose();
    }

    /**
     * Конструктор об'єкта моделі.
     * @param host адреса сервера
     * @param port порт сервера
     * @param name ім'я користувача
     * @param password пароль користувача
     */
    public Client(final String host, final int port, final String name, final String password) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.password = password;
    }

    /**
     * Підключає до сервера.
     * @param onMessageSendCallback обробник успішного надіслання повідомлення
     * @param onMessageSendFailCallback обробник невдалого надіслання повідомлення
     * @param onMessageReceivedCallback обробник отримання звичайного повідомлення
     * @param onForceCloseCallback обробник примусового відключення від сервера
     * @throws IOException якщо сталася помилка під час роботи із сокетами або
     *         IO-потоками
     */
    public void connect(final OnMessageSendCallback onMessageSendCallback,
                        final OnMessageSendFailCallback onMessageSendFailCallback,
                        final OnMessageReceivedCallback onMessageReceivedCallback,
                        final OnForceCloseCallback onForceCloseCallback) throws IOException {
        if (!active) {
            clientSocket = new Socket(host, port);
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.onMessageSendCallback = onMessageSendCallback;
            this.onMessageSendFailCallback = onMessageSendFailCallback;
            this.onMessageReceivedCallback = onMessageReceivedCallback;
            this.onForceCloseCallback = onForceCloseCallback;
            active = true;
            new Thread(this).start();

            // перше повідомлення буде містити авторизаційні дані
            Message message = new Message();
            message.setType(Message.Type.NOTIFICATION);
            message.setStatus(Message.NotificationStatus.AUTH);
            message.setName(name);
            message.setContent(password);
            sendMessage(message);
        }
    }

    /**
     * Відключає від сервера.
     * @throws IOException якщо сталася помилка під час роботи із сокетом
     */
    public void disconnect() throws IOException {
        if (active) {
            active = false;

            Message message = new Message();
            message.setType(Message.Type.NOTIFICATION);
            message.setStatus(Message.NotificationStatus.CLOSING);
            sendMessage(message);

            clientSocket.close();
        }
    }

    /**
     * Надсилає повідомлення на сервер.
     * @param message об'єкт повідомлення
     */
    public synchronized void sendMessage(final Message message) {
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            onMessageSend(message);
        } catch (IOException e) {
            // клієнт припиняє роботу
            active = false;
            try {
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            onMessageSendFail(message);
        }
    }

    /**
     * Основна логіка клієнта, виконується в разі його активності.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && active) {
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
                Thread.sleep(LISTENING_PERIOD);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Повертає адресу сервера.
     * @return адреса сервера
     */
    public String getHost() {
        return host;
    }

    /**
     * Повертає порт сервера.
     * @return порт сервера
     */
    public int getPort() {
        return port;
    }

    /**
     * Повертає IP клієнта.
     * @return IP клієнта
     */
    public String getIP() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(IP_TEST_HOST), IP_TEST_PORT);
            return socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            return "0.0.0.0";
        }
    }

    /**
     * Повертає стан активності клієнта.
     * @return стан активності клієнта
     */
    public boolean isActive() {
        return active;
    }
}
