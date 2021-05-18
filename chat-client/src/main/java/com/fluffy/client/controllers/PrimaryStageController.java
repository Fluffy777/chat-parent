package com.fluffy.client.controllers;

import com.fluffy.client.exceptions.ClientConnectionException;
import com.fluffy.client.exceptions.ClientDisconnectionException;
import com.fluffy.client.services.ClientService;
import com.fluffy.controllers.AbstractStageController;
import com.fluffy.messaging.Message;
import com.fluffy.util.AlertUtil;
import com.fluffy.util.ApplicationContext;
import com.fluffy.util.Environment;
import com.fluffy.util.MessageStatusMapperUtil;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Клас контролера головного вікна програми.
 * @author Сивоконь Вадим
 */
public class PrimaryStageController extends AbstractStageController {
    /**
     * Сервіс для отримання даних про клієнтів.
     */
    private final ClientService clientService;

    /**
     * Головне вікно програми.
     */
    private final Stage primaryStage;

    /**
     * Текст, містить інформацію про сервер.
     */
    @FXML
    private Text serverText;

    /**
     * Текст, містить ім'я користувача.
     */
    @FXML
    private Text nameText;

    /**
     * Випадаючий список для обрання статусу користувача.
     */
    @FXML
    private ComboBox<String> statusComboBox;

    /**
     * Область для введення повідомлення.
     */
    @FXML
    private TextArea messageTextArea;

    /**
     * Кнопка надіслання повідомлення.
     */
    @FXML
    private Button sendButton;

    /**
     * Список повідомлень.
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
         * Ім'я, що буде відображатися, якщо звичайне повідомлення надсилає
         * сервер.
         */
        private static final String SERVER_NAME = Environment.getProperty("gui.server-name");

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
            statusLabel = new Label();

            contentLabel = new Label();
            contentLabel.setMaxWidth(CONTENT_MAX_WIDTH);
            contentLabel.setWrapText(true);

            container = new HBox(timeLabel, userNameLabel, statusLabel, contentLabel);
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
        // відмалювання повідомлення відбудеться лише після факту його
        // надіслання (додатково відбувається очищення області для введення
        // тексту)
        messageTextArea.setText("");
        addMessage(message);
    }

    private void onMessageSendFail(final Message message) {
        // сервіс передбачає, що в разі виникнення помилки клієнт буде
        // відключений автоматично, тому треба лише узгодити це з відображенням
        onConnectionStateChange(false);

        // відображаємо повідомлення про те, що сталося
        AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Не вдалося надіслати повідомлення");
    }

    private void onMessageReceived(final Message message) {
        addMessage(message);
    }

    private void onForceClose() {
        try {
            clientService.disconnect();
            onConnectionStateChange(false);
            AlertUtil.show(Alert.AlertType.INFORMATION, "Повідомлення", "Повідомлення", "Сервер зупинив з'єднання");
        } catch (ClientDisconnectionException e) {
            onConnectionStateChange(false);
            AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка", "Сервер зупинив з'єднання, а клієнт не зміг після цього відключитися коректно");
        }
    }

    private void onConnectionStateChange(final boolean connected) {
        serverText.setText(clientService.getServerInfo());
        messageTextArea.setDisable(!connected);
        sendButton.setDisable(!connected);
        statusComboBox.setDisable(!connected);

        if (!connected) {
            statusComboBox.setValue(null);
            nameText.setText("");
            messageTextArea.setText("");
            historyListView.getItems().clear();
        }
    }

    /**
     * Конструктор об'єкта контролера.
     */
    public PrimaryStageController() {
        primaryStage = (Stage) ApplicationContext.lookup("primaryStage");
        clientService = (ClientService) ApplicationContext.lookup("clientService");

        // на випадок спроби закриття головного вікна - буде показане вікно для
        // підтвердження
        primaryStage.setOnCloseRequest(event -> {
            Optional<ButtonType> result = AlertUtil.show(Alert.AlertType.CONFIRMATION, "Питання", "Питання", "Закрити програму?");
            if (result.isPresent() && result.get().equals(AlertUtil.OK_BUTTON)) {
                // завершення підключення до сервера, якщо воно наявне
                if (clientService.isClientActive()) {
                    try {
                        clientService.disconnect();
                        onConnectionStateChange(false);
                    } catch (ClientDisconnectionException e) {
                        AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка відключення від сервера", e.getMessage());
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

        List<String> statuses = new LinkedList<>();
        for (Message.Status status : Message.Status.values()) {
            statuses.add(status.getGUIString());
        }
        statusComboBox.getItems().setAll(statuses);
        statusComboBox.setValue(Message.Status.NONE.getGUIString());

        clientService.initCallbacks((message) -> {
            Platform.runLater(() -> {
                PrimaryStageController.this.onMessageSend(message);
            });
        }, (message) -> {
            // повідомлення не вдалося надіслати, тому відображаємо
            // попередження та відключаємо клієнта
            Platform.runLater(() -> {
                PrimaryStageController.this.onMessageSendFail(message);
            });
        }, (message) -> {
            Platform.runLater(() -> {
                PrimaryStageController.this.onMessageReceived(message);
            });
        }, () -> {
            Platform.runLater(() -> {
                PrimaryStageController.this.onForceClose();
            });
        });
    }

    /**
     * Обробник натискання на кнопку меню для під'єднання до сервера.
     */
    public void connectMenuItemOnAction() {
        ClientConnectionStageController clientConnectionStageController =
                (ClientConnectionStageController) ApplicationContext.lookup("clientConnectionStageController");
        clientConnectionStageController.showViewAndWait();
        if (lastResponse != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> temp = (Map<String, Object>) lastResponse;

                clientService.connect((String) temp.get("host"), (Integer) temp.get("port"),
                        (String) temp.get("name"), (String) temp.get("password"));

                nameText.setText((String) temp.get("name"));
                onConnectionStateChange(true);
            } catch (ClientConnectionException e) {
                AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка підключення до сервера", e.getMessage());
            }
        }
    }

    /**
     * Обробник натискання на кнопку меню для від'єднання від сервера.
     */
    public void disconnectMenuItemOnAction() {
        try {
            clientService.disconnect();
            onConnectionStateChange(false);
        } catch (ClientDisconnectionException e) {
            AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка відключення від сервера", e.getMessage());
        }
    }

    /**
     * Обробник натискання на кнопку для надіслання повідомлення.
     */
    public void sendButtonOnAction() {
        String text = messageTextArea.getText();
        if (text.matches("^\\s*$")) {
            AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Текст повідомлення порожній");
        } else {
            Message message = new Message();
            message.setType(Message.Type.TEXT);
            message.setName(nameText.getText());
            message.setContent(text);

            String statusGUIString = statusComboBox.getSelectionModel().getSelectedItem();
            if (statusGUIString != null) {
                message.setStatus(MessageStatusMapperUtil.mapToStatus(statusGUIString));
            }
            message.setDateTime(LocalDateTime.now());
            message.setIp(clientService.getIP());

            clientService.sendMessage(message);
        }
    }
}
