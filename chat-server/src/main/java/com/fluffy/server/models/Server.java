package com.fluffy.server.models;

import com.fluffy.callbacks.OnConnectionsCountChangedCallback;
import com.fluffy.callbacks.OnMessageReceivedCallback;
import com.fluffy.messaging.Message;
import com.fluffy.util.Environment;
import com.fluffy.util.LimitedLinkedList;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Клас моделі сервера, об'єкти якого підтримують зв'язок із клієнтами.
 * @author Сивоконь Вадим
 */
public class Server implements Runnable {
    /**
     * Період (у мілісекундах) перевірки наявності підключення від клієнта до
     * сервера.
     */
    private static final int KEEP_ALIVE_PERIOD = Integer.parseInt(Environment.getProperty("server.keep-alive-period"));

    /**
     * Період (у мілісекундах) отримання нових з'єднань.
     */
    private static final int ACCEPTANCE_PERIOD = Integer.parseInt(Environment.getProperty("server.acceptance-period"));

    /**
     * Максимальна кількість повідомлень для збереження історії.
     */
    private static final int LAST_MESSAGES_LIMIT = Integer.parseInt(Environment.getProperty("server.last-messages-limit"));

    /**
     * Максимальна кількість підключень до сервера.
     */
    public static final int CONNECTIONS_LIMIT = Integer.parseInt(Environment.getProperty("server.connections-limit"));

    /**
     * Адреса для отримання IP сервера.
     */
    private static final String IP_TEST_HOST = Environment.getProperty("server.ip-test-host");

    /**
     * Порт для отримання IP сервера.
     */
    private static final int IP_TEST_PORT = Integer.parseInt(Environment.getProperty("server.ip-test-port"));

    /**
     * Порт.
     */
    private final int port;

    /**
     * Сокет сервера.
     */
    private ServerSocket serverSocket;

    /**
     * Список підключених клієнтів.
     */
    private List<ClientHandler> clients;

    /**
     * Стан сервера.
     */
    private boolean active;

    /**
     * Список останніх повідомлень.
     */
    private Queue<Message> lastMessages;

    /**
     * Обробник отримання звичайного повідомлення.
     */
    private OnMessageReceivedCallback onMessageReceivedCallback;

    /**
     * Обробник зміни кількості підключень.
     */
    private OnConnectionsCountChangedCallback onConnectionsCountChangedCallback;

    /**
     * Конструктор об'єкта сервера.
     * @param port порт
     */
    public Server(final int port) {
        this.port = port;
    }

    /**
     * Запускає сервер.
     * @param onMessageReceivedCallback обробник отримання звичайного повідомлення
     * @param onConnectionsCountChangedCallback обробник зміни кількості підключень
     * @throws IOException якщо це не вдалося зробити запуск
     */
    public void startup(final OnMessageReceivedCallback onMessageReceivedCallback,
                        final OnConnectionsCountChangedCallback onConnectionsCountChangedCallback) throws IOException {
        if (!active) {
            serverSocket = new ServerSocket(port);
            clients = new LinkedList<>();
            lastMessages = new LimitedLinkedList<>(LAST_MESSAGES_LIMIT);
            this.onMessageReceivedCallback = onMessageReceivedCallback;
            this.onConnectionsCountChangedCallback = onConnectionsCountChangedCallback;
            active = true;
            new Thread(this).start();

            // перевірка існування з'єднань
            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted() && active) {
                    Message message = new Message();
                    message.setType(Message.Type.NOTIFICATION);
                    message.setStatus(Message.NotificationStatus.KEEPING_ALIVE);
                    sendMessage(message);

                    try {
                        Thread.sleep(KEEP_ALIVE_PERIOD);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }).start();
        }
    }

    /**
     * Зупиняє сервер.
     * @throws IOException якщо не вдалося зупинити сервер
     */
    public void shutdown() throws IOException {
        if (active) {
            serverSocket.close();

            for (ClientHandler client : clients) {
                client.close();
            }

            active = false;
        }
    }

    // Обробники подій для виконання додаткової логіки на рівні сервера

    private void onMessageReceived() {
    }

    private void onConnectionsCountChanged(final int newSize) {
        onConnectionsCountChangedCallback.onConnectionsCountChanged(newSize);
    }

    /**
     * Виконує основну логіку сервера в окремому потоці.
     */
    @Override
    public void run() {
        Socket clientSocket;

        while (!Thread.currentThread().isInterrupted() && active) {
            try {
                clientSocket = serverSocket.accept();
                final ClientHandler client = new ClientHandler(clientSocket, this, (message) -> {
                    if (Server.this.isActive()) {
                        Server.this.onMessageReceived();
                        onMessageReceivedCallback.onMessageReceived(message);
                    }
                });
                clients.add(client);
                onConnectionsCountChanged(clients.size());
                new Thread(client).start();
            } catch (IOException e) {
                // клієнту не вдалося здійснити підключення
            }

            try {
                Thread.sleep(ACCEPTANCE_PERIOD);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Надсилає повідомлення лише одному клієнту.
     * @param theOnlyClient клієнт
     * @param message повідомлення
     */
    public void sendMessage(final ClientHandler theOnlyClient, final Message message) {
        try {
            theOnlyClient.sendMessage(message);
        } catch (IOException e) {
            // не вдалося надіслати повідомлення - можливо, клієнт від'єднався
            int previousSize = clients.size();
            clients.remove(theOnlyClient);
            int newSize = clients.size();
            if (newSize != previousSize) {
                onConnectionsCountChanged(newSize);
            }
        }
    }

    /**
     * Надсилає повідомлення всім клієнтам.
     * @param message повідомлення
     */
    public void sendMessage(final Message message) {
        List<ClientHandler> invalidClients = new LinkedList<>();
        int previousSize = clients.size();

        if (!message.getType().equals(Message.Type.NOTIFICATION)) {
            lastMessages.add(message);
        }
        for (ClientHandler client : clients) {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                // не вдалося надіслати повідомлення - можливо, клієнт від'єднався
                invalidClients.add(client);
            }
        }

        clients.removeAll(invalidClients);
        int newSize = clients.size();
        if (newSize != previousSize) {
            onConnectionsCountChanged(newSize);
        }
    }

    /**
     * Надсилає повідомлення всім клієнтам за винятком одного.
     * @param message повідомлення
     * @param other клієнт-виняток
     */
    public void sendMessage(final Message message, final ClientHandler other) {
        List<ClientHandler> invalidClients = new LinkedList<>();
        int previousSize = clients.size();

        if (!message.getType().equals(Message.Type.NOTIFICATION)) {
            lastMessages.add(message);
        }
        for (ClientHandler client : clients) {
            if (!client.equals(other)) {
                try {
                    client.sendMessage(message);
                } catch (IOException e) {
                    // не вдалося надіслати повідомлення - можливо, клієнт від'єднався
                    invalidClients.add(client);
                }
            }
        }

        clients.removeAll(invalidClients);
        int newSize = clients.size();
        if (newSize != previousSize) {
            onConnectionsCountChanged(newSize);
        }
    }

    /**
     * Надсилає останні повідомлення одному клієнту.
     * @param client клієнт
     */
    public void sendLastMessages(final ClientHandler client) {
        try {
            for (Message message : lastMessages) {
                client.sendMessage(message);
            }
        } catch (IOException e) {
            clients.remove(client);
            onConnectionsCountChanged(clients.size());
        }
    }

    /**
     * Видаляє клієнта зі списку підключених.
     * @param client клієнт
     */
    public void removeClient(final ClientHandler client) {
        clients.remove(client);
        onConnectionsCountChanged(clients.size());
    }

    /**
     * Відключає клієнта за його іменем від сервера.
     * @param name ім'я
     */
    public void disconnectClientByName(final String name) {
        ClientHandler client = null;
        for (ClientHandler temp : clients) {
            if (temp.getName().equals(name)) {
                client = temp;
                break;
            }
        }

        if (client != null) {
            Message message = new Message();
            message.setType(Message.Type.NOTIFICATION);
            message.setStatus(Message.NotificationStatus.FORCE_CLOSE);
            sendMessage(client, message);
            client.close();
        }
    }

    /**
     * Повертає порт, на якому запущений сервер.
     * @return порт
     */
    public int getPort() {
        return port;
    }

    /**
     * Повертає IP-адресу сервера.
     * @return IP-адреса сервера
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
     * Повертає логічне значення - чи є сервер активним у момент виклику.
     * @return чи є сервер активним
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Повертає поточну кількість підключених клієнтів.
     * @return поточна кількість підключених клієнтів
     */
    public int getClientsCount() {
        return clients.size();
    }
}
