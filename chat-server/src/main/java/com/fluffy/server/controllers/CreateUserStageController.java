package com.fluffy.server.controllers;

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
 * Клас контролера модального вікна для створення користувача.
 * @author Сивоконь Вадим
 */
public class CreateUserStageController extends AbstractStageController {
    /**
     * Модальне вікно створення користувача.
     */
    private final Stage createUserStage;

    /**
     * Контролер головного вікна.
     */
    private final PrimaryStageController primaryStageController;

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
    public CreateUserStageController() {
        createUserStage = (Stage) ApplicationContext.lookup("createUserStage");
        primaryStageController = (PrimaryStageController) ApplicationContext.lookup("primaryStageController");

        createUserStage.setOnCloseRequest(event -> {
            primaryStageController.setLastResponse(null);
        });
    }

    /**
     * Обробник натискання кнопки для створення користувача.
     */
    public void createButtonOnAction() {
        String name = nameTextField.getText();
        if (name.matches("^\\s*$")) {
            AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Ім'я не може бути порожнім");
        } else {
            // ім'я не порожнє
            String password = passwordTextField.getText();

            // дозволимо використання паролів, що містить лише пробіли
            if (password.isEmpty()) {
                AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Пароль не вказаний");
            } else {
                // пароль відповідає вимогам
                nameTextField.setText("");
                passwordTextField.setText("");

                Map<String, Object> response = new HashMap<>();
                response.put("name", name);
                response.put("password", password);

                primaryStageController.setLastResponse(response);
                createUserStage.close();
            }
        }
    }

    /**
     * Відображає вікно для очікування відповіді.
     */
    public void showViewAndWait() {
        createUserStage.showAndWait();
    }
}
