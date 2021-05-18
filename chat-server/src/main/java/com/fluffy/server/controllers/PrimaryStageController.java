package com.fluffy.server.controllers;

import com.fluffy.controllers.AbstractStageController;
import com.fluffy.messaging.Message;
import com.fluffy.server.exceptions.DBConnectionException;
import com.fluffy.server.exceptions.PersistException;
import com.fluffy.server.exceptions.ServerShutdownException;
import com.fluffy.server.exceptions.ServerStartupException;
import com.fluffy.server.models.User;
import com.fluffy.server.services.ServerService;
import com.fluffy.server.services.UserService;
import com.fluffy.util.AlertUtil;
import com.fluffy.util.ApplicationContext;
import com.fluffy.util.Environment;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Клас контролера, що відповідає за реагування на дії користувача в головному
 * вікні додатку.
 * @author Сивоконь Вадим
 */
public class PrimaryStageController extends AbstractStageController {
    /**
     * Ім'я, що буде відображатися, якщо звичайне повідомлення надсилає
     * сервер.
     */
    private static final String SERVER_NAME = Environment.getProperty("gui.server-name");

    /**
     * Сервіс для роботи із сервером.
     */
    private final ServerService serverService;

    /**
     * Сервіс для отримання даних про користувачів.
     */
    private final UserService userService;

    /**
     * Головне вікно додатку.
     */
    private final Stage primaryStage;

    /**
     * Текст, містить інформацію про запущений сервер.
     */
    @FXML
    private Text serverText;

    /**
     * Текст, містить інформацію про клієнтів (їх кількість).
     */
    @FXML
    private Text clientsText;

    /**
     * Текст, містить назву сервера.
     */
    @FXML
    private Text nameText;

    /**
     * Текстова область для введення повідомлення.
     */
    @FXML
    private TextArea messageTextArea;

    /**
     * Кнопка для надіслання повідомлення від сервера.
     */
    @FXML
    private Button sendButton;

    /**
     * Список надісланих повідомлень.
     */
    @FXML
    private ListView<HBox> historyListView;

    /**
     * Клас графічного елемента, що представляє текстове повідомлення. Є
     * внутрішним класом, оскільки відображення на сервері та клієнті
     * може відрізнятися.
     */
    private static final class MessageElement {
        /**
         * Стиль відображення імені сервера в повідомленнях.
         */
        private static final String SERVER_NAME_STYLE = Environment.getProperty("gui.server-name-style");

        /**
         * Префікс для пошуку зображень статусів.
         */
        private static final String STATUS_IMAGE_PREFIX = Environment.getProperty("gui.message-status-image-prefix");

        /**
         * Суфікс для пошуку зображень статусів.
         */
        private static final String STATUS_IMAGE_SUFFIX = Environment.getProperty("gui.message-status-image-suffix");

        /**
         * Ширина зображення статусу.
         */
        private static final double STATUS_IMAGE_WIDTH = Double.parseDouble(Environment.getProperty("gui.message-status-image-width"));

        /**
         * Висота зображення статусу.
         */
        private static final double STATUS_IMAGE_HEIGHT = Double.parseDouble(Environment.getProperty("gui.message-status-image-height"));

        /**
         * Шаблон для форматування часу.
         */
        private static final String TIME_PATTERN = Environment.getProperty("gui.message-time-pattern");

        /**
         * Максимальна довжина напису із текстом повідомлення.
         */
        private static final double CONTENT_MAX_WIDTH = Double.parseDouble(Environment.getProperty("gui.message-content-max-width"));

        /**
         * Відстань між елементами контейнера.
         */
        private static final double CONTAINER_SPACING = Double.parseDouble(Environment.getProperty("gui.message-content-spacing"));

        /**
         * Об'єкт повідомлення.
         */
        private final Message message;

        /**
         * Напис із часом надіслання.
         */
        private final Label timeLabel;

        /**
         * Напис із іменем клієнта.
         */
        private final Label userNameLabel;

        /**
         * Напис із IP клієнта.
         */
        private final Label ipLabel;

        /**
         * Напис із статусом у вигляді зображення.
         */
        private final Label statusLabel;

        /**
         * Напис із текстом повідомлення.
         */
        private final Label contentLabel;

        /**
         * Контейнер графічного елемента.
         */
        private final HBox container;

        /**
         * Створює об'єкт графічного елемента для повідомлення.
         * @param message повідомлення
         */
        MessageElement(final Message message) {
            this.message = message;

            timeLabel = new Label();
            userNameLabel = new Label();
            ipLabel = new Label();
            statusLabel = new Label();

            contentLabel = new Label();
            contentLabel.setMaxWidth(CONTENT_MAX_WIDTH);
            contentLabel.setWrapText(true);

            container = new HBox(timeLabel, userNameLabel, ipLabel, statusLabel, contentLabel);
            container.setSpacing(CONTAINER_SPACING);
        }

        /**
         * Оновлює графічний елемент відповідно до пов'язаного із ним об'єктом
         * повідомлення.
         */
        public void update() {
            timeLabel.setText(DateTimeFormatter.ofPattern(TIME_PATTERN).format(message.getDateTime()));

            String name = message.getName();
            userNameLabel.setText(name);
            if (name.equalsIgnoreCase(SERVER_NAME)) {
                userNameLabel.setStyle(SERVER_NAME_STYLE);
            }
            ipLabel.setText(message.getIp());

            Message.Status status = (Message.Status) message.getStatus();
            if (status != null && !status.equals(Message.Status.NONE)) {
                statusLabel.setGraphic(new ImageView(new Image(STATUS_IMAGE_PREFIX + status.toString().toLowerCase() + STATUS_IMAGE_SUFFIX, STATUS_IMAGE_WIDTH, STATUS_IMAGE_HEIGHT, true, true)));
            }
            contentLabel.setText(message.getContent());
        }

        /**
         * Повертає об'єкт повідомлення.
         * @return повідомлення
         */
        public Message getMessage() {
            return message;
        }
    }

    // Допоміжні методи для роботи із GUI

    private void addMessage(final Message message) {
        ObservableList<HBox> elements = historyListView.getItems();
        MessageElement messageElement = new MessageElement(message);
        messageElement.update();
        elements.add(messageElement.container);
    }

    // Обробники подій

    private void onMessageSend(final Message message) {
        messageTextArea.setText("");
        addMessage(message);
    }

    private void onMessageSendFail(final Message message) {
        AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка", "Не вдалося надіслати повідомлення: сервер не запущений");
    }

    private void onMessageReceived(final Message message) {
        // перевірка на тип не є необхідною, оскільки в разі, якщо повідомлення
        // є спеціальним - виконання цього методу буде уникнуте (callback не
        // буде викликаний)
        addMessage(message);
    }

    private void onConnectionsCountChanged(final int newCount) {
        // можна скористатися значенням newCount або відформатованим
        // результатом від сервісу
        clientsText.setText(serverService.getClientsInfo());
    }

    private void onConnectionStateChange(final boolean connected) {
        // якщо !connected - сервіс поверне "" самостійно
        serverText.setText(serverService.getServerInfo());
        clientsText.setText(serverService.getClientsInfo());
        messageTextArea.setDisable(!connected);
        sendButton.setDisable(!connected);

        if (!connected) {
            nameText.setText("");
            messageTextArea.setText("");
            historyListView.getItems().clear();
        } else {
            nameText.setText(SERVER_NAME);
        }
    }

    /**
     * Конструктор об'єкта контролера.
     */
    public PrimaryStageController() {
        primaryStage = (Stage) ApplicationContext.lookup("primaryStage");
        serverService = (ServerService) ApplicationContext.lookup("serverService");
        userService = (UserService) ApplicationContext.lookup("userService");

        primaryStage.setOnCloseRequest(event -> {
            Optional<ButtonType> result = AlertUtil.show(Alert.AlertType.CONFIRMATION, "Питання", "Питання", "Закрити програму?");
            if (result.isPresent() && result.get().equals(AlertUtil.OK_BUTTON)) {
                // завершення роботи із сервером, якщо той був запущений
                if (serverService.isServerActive()) {
                    try {
                        serverService.shutdownServer();
                        onConnectionStateChange(false);
                    } catch (ServerShutdownException e) {
                        AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка зупинки сервера", e.getMessage());
                    }
                }
                Platform.exit();
                System.exit(0);
            } else {
                event.consume();
            }
        });
    }

    @FXML
    private void initialize() {
        historyListView.getItems().addListener(new ListChangeListener<HBox>() {
            @Override
            public void onChanged(final javafx.collections.ListChangeListener.Change<? extends HBox> c) {
                historyListView.scrollTo(c.getList().size() - 1);
            }
        });

        serverService.initCallbacks((message) -> {
            Platform.runLater(() -> {
                PrimaryStageController.this.onMessageSend(message);
            });
        }, (message) -> {
            Platform.runLater(() -> {
                PrimaryStageController.this.onMessageSendFail(message);
            });
        }, (message) -> {
            Platform.runLater(() -> {
                PrimaryStageController.this.onMessageReceived(message);
            });
        }, (newCount) -> {
            Platform.runLater(() -> {
                PrimaryStageController.this.onConnectionsCountChanged(newCount);
            });
        });
    }

    /**
     * Обробник натискання на кнопку меню для запуску сервера.
     */
    public void startupMenuItemOnAction() {
        ServerStartupStageController serverStartupStageController = (ServerStartupStageController) ApplicationContext.lookup("serverStartupStageController");
        serverStartupStageController.showViewAndWait();
        if (lastResponse != null) {
            try {
                serverService.startupServer((Integer) lastResponse);
                onConnectionStateChange(true);
            } catch (ServerStartupException e) {
                AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка запуску сервера", e.getMessage());
            }
        }
    }

    /**
     * Обробник натискання на кнопку меню для зупинки сервера.
     */
    public void shutdownMenuItemOnAction() {
        try {
            serverService.shutdownServer();
            onConnectionStateChange(false);
        } catch (ServerShutdownException e) {
            AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка зупинки сервера", e.getMessage());
        }
    }

    /**
     * Обробник натискання на кнопку для відправки повідомлення від сервера.
     */
    public void sendButtonOnAction() {
        String text = messageTextArea.getText();
        if (text.matches("^\\s*$")) {
            AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Текст повідомлення порожній");
        } else {
            Message message = new Message();
            message.setType(Message.Type.TEXT);
            message.setName(SERVER_NAME);
            message.setContent(text);
            message.setDateTime(LocalDateTime.now());
            message.setIp(serverService.getIP());

            serverService.sendMessage(message);
        }
    }

    /**
     * Обробник натискання на кнопку меню для створення користувача.
     */
    public void createUserMenuItemOnAction() {
        CreateUserStageController createUserStageController = (CreateUserStageController) ApplicationContext.lookup("createUserStageController");
        createUserStageController.showViewAndWait();
        if (lastResponse != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> temp = (Map<String, Object>) lastResponse;

            // id не треба встановлювати - він згенерується самостійно
            User user = new User();
            user.setName((String) temp.get("name"));
            user.setPassword(BCrypt.hashpw((String) temp.get("password"), BCrypt.gensalt()));

            try {
                userService.create(user);
            } catch (DBConnectionException e) {
                AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка", e.getMessage());
            } catch (PersistException e) {
                AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", e.getMessage());
            }
        }
    }

    /**
     * Обробник натискання на кнопку меню для оновлення користувача.
     */
    public void updateUserMenuItemOnAction() {
        UpdateUserStageController updateUserStageController = (UpdateUserStageController) ApplicationContext.lookup("updateUserStageController");
        updateUserStageController.showAndWait();
        if (lastResponse != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> temp = (Map<String, Object>) lastResponse;

            try {
                User user = userService.findByName((String) temp.get("name"));
                User newUser = new User();
                // id не треба вказувати, оскільки буде взятий id від user
                newUser.setName((String) temp.get("newName"));
                newUser.setPassword(BCrypt.hashpw((String) temp.get("newPassword"), BCrypt.gensalt()));

                userService.update(newUser, user.getId());

                if (serverService.isServerActive()) {
                    serverService.disconnectClientByName(newUser.getName());
                }
            } catch (DBConnectionException e) {
                AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка", e.getMessage());
            } catch (PersistException e) {
                AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", e.getMessage());
            }
        }
    }

    /**
     * Обробник натискання на кнопку меню для видалення користувача.
     */
    public void deleteUserMenuItemOnAction() {
        DeleteUserStageController deleteUserStageController = (DeleteUserStageController) ApplicationContext.lookup("deleteUserStageController");
        deleteUserStageController.showAndWait();

        if (lastResponse != null) {
            try {
                User user = userService.findByName((String) lastResponse);
                userService.delete(user.getId());

                if (serverService.isServerActive()) {
                    serverService.disconnectClientByName(user.getName());
                }
            } catch (DBConnectionException e) {
                AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка", e.getMessage());
            } catch (PersistException e) {
                AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", e.getMessage());
            }
        }
    }
}
