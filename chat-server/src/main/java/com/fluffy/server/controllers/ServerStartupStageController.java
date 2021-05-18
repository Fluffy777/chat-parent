package com.fluffy.server.controllers;

import com.fluffy.controllers.AbstractStageController;
import com.fluffy.util.AlertUtil;
import com.fluffy.util.ApplicationContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Клас контролера модального вікна для запуску сервера.
 * @author Сивоконь Вадим
 */
public class ServerStartupStageController extends AbstractStageController {
    /**
     * Шаблон для перевірки порту.
     */
    private static final String PORT_PATTERN = "^\\d{1,5}$";

    /**
     * Максимальне значення порту.
     */
    private static final int PORT_MAX_VALUE = 65535;

    /**
     * Модальне вікно запуску сервера.
     */
    private final Stage serverStartupStage;

    /**
     * Контролер головного вікна.
     */
    private final PrimaryStageController primaryStageController;

    /**
     * Текстове поле для введення порту.
     */
    @FXML
    private TextField portTextField;

    /**
     * Конструктор об'єкта контролера.
     */
    public ServerStartupStageController() {
        serverStartupStage = (Stage) ApplicationContext.lookup("serverStartupStage");
        primaryStageController = (PrimaryStageController) ApplicationContext.lookup("primaryStageController");

        serverStartupStage.setOnCloseRequest(event -> {
            primaryStageController.setLastResponse(null);
        });
    }

    /**
     * Обробник натискання на кнопку запуску сервера.
     */
    public void startupButtonOnAction() {
        String port = portTextField.getText();
        if (port.isEmpty()) {
            AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка запуску сервера", "Порт не вказаний");
        } else if (!port.matches(PORT_PATTERN)) {
            AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка запуску сервера", "Порт не відповідає формату");
        } else {
            int result = Integer.parseInt(port);
            if (result > PORT_MAX_VALUE) {
                AlertUtil.show(Alert.AlertType.ERROR, "Помилка", "Помилка запуску сервера", "Значення порту більше за максимально можливе (" + PORT_MAX_VALUE + ")");
            } else {
                primaryStageController.setLastResponse(result);
                serverStartupStage.close();
            }
        }
    }

    /**
     * Відображає вікно для очікування відповіді.
     */
    public void showViewAndWait() {
        serverStartupStage.showAndWait();
    }
}
