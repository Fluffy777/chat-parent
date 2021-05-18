package com.fluffy.client.controllers;

import com.fluffy.controllers.AbstractStageController;
import com.fluffy.util.AlertUtil;
import com.fluffy.util.ApplicationContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Клас контролера вікна підключення клієнта до сервера.
 * @author Сивоконь Вадим
 */
public class ClientConnectionStageController extends AbstractStageController {
    /**
     * Шаблон для перевірки порту.
     */
    private static final String PORT_PATTERN = "^\\d{1,5}$";

    /**
     * Максимальне значення порту.
     */
    private static final int PORT_MAX_VALUE = 65535;

    /**
     * Вікно підключення клієнта до сервера.
     */
    private final Stage clientConnectionStage;

    /**
     * Контролер головного вікна.
     */
    private final PrimaryStageController primaryStageController;

    /**
     * Текстове поле для введення адреси сервера.
     */
    @FXML
    private TextField hostTextField;

    /**
     * Текстове поле для введення порту.
     */
    @FXML
    private TextField portTextField;

    /**
     * Текстове поле для введення імені користувача.
     */
    @FXML
    private TextField nameTextField;

    /**
     * Текстове поле для введення пароля користувача.
     */
    @FXML
    private TextField passwordTextField;

    /**
     * Конструктор об'єкта контролера.
     */
    public ClientConnectionStageController() {
        clientConnectionStage = (Stage) ApplicationContext.lookup("clientConnectionStage");
        primaryStageController = (PrimaryStageController) ApplicationContext.lookup("primaryStageController");

        // якщо вікно закриватимуть - головному повернеться порожня відповідь
        clientConnectionStage.setOnCloseRequest(event -> {
            primaryStageController.setLastResponse(null);
        });
    }

    /**
     * Обробник натискання на кнопку підключення.
     */
    public void connectButtonOnAction() {
        String host = hostTextField.getText();
        if (host.isEmpty()) {
            AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка підключення до сервера", "Адреса сервера не вказана");
        } else {
            // адреса сервера непорожня
            String port = portTextField.getText();
            if (port.isEmpty()) {
                AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка підключення до сервера", "Порт не вказаний");
            } else if (!port.matches(PORT_PATTERN)) {
                AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка підключення до сервера", "Порт не відповідає формату");
            } else {
                // порт відповідає формату
                int result = Integer.parseInt(port);
                if (result > PORT_MAX_VALUE) {
                    AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка підключення до сервера", "Значення порту більше за максимально можливе (" + PORT_MAX_VALUE + ")");
                } else {
                    // порт відповідає вимогам

                    // перевірка даних авторизації
                    String name = nameTextField.getText();
                    if (name.matches("^\\s*$")) {
                        AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Ім'я не може бути порожнім");
                    } else {
                        // ім'я відповідає вимогам
                        String password = passwordTextField.getText();
                        if (password.isEmpty()) {
                            AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Пароль не вказаний");
                        } else {
                            // пароль відповідає вимогам
                            Map<String, Object> response = new HashMap<>();
                            response.put("host", host);
                            response.put("port", result);
                            response.put("name", name);
                            response.put("password", password);

                            primaryStageController.setLastResponse(response);
                            clientConnectionStage.close();
                        }
                    }
                }
            }
        }
    }

    /**
     * Відображає вікно для очікування відповіді.
     */
    public void showViewAndWait() {
        clientConnectionStage.showAndWait();
    }
}
