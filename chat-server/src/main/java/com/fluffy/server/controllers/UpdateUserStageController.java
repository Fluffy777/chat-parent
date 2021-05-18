package com.fluffy.server.controllers;

import com.fluffy.controllers.AbstractStageController;
import com.fluffy.server.exceptions.DBConnectionException;
import com.fluffy.server.exceptions.PersistException;
import com.fluffy.server.models.User;
import com.fluffy.server.services.UserService;
import com.fluffy.util.AlertUtil;
import com.fluffy.util.ApplicationContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Клас контролера модального вікна для оновлення користувача.
 * @author Сивоконь Вадим
 */
public class UpdateUserStageController extends AbstractStageController {
    /**
     * Модальне вікно оновлення користувача.
     */
    private final Stage updateUserStage;

    /**
     * Контролер головного вікна.
     */
    private final PrimaryStageController primaryStageController;

    /**
     * Сервіс для отримання даних про користувачів.
     */
    private final UserService userService;

    /**
     * Випадаючий список для обрання імені користувача, дані про якого треба
     * оновити.
     */
    @FXML
    private ComboBox<String> nameComboBox;

    /**
     * Текстове поле для введення нового імені користувача.
     */
    @FXML
    private TextField newNameTextField;

    /**
     * Текстове поле для введення нового пароля користувача.
     */
    @FXML
    private TextField newPasswordTextField;

    /**
     * Конструктор об'єкта контролера.
     */
    public UpdateUserStageController() {
        updateUserStage = (Stage) ApplicationContext.lookup("updateUserStage");
        primaryStageController = (PrimaryStageController) ApplicationContext.lookup("primaryStageController");
        userService = (UserService) ApplicationContext.lookup("userService");

        updateUserStage.setOnCloseRequest(event -> {
            primaryStageController.setLastResponse(null);
        });
    }

    private void updateNameComboBox() {
        List<User> users;
        try {
            users = userService.findAll();
        } catch (DBConnectionException | PersistException e) {
            users = new LinkedList<>();
        }
        List<String> names = new LinkedList<>();

        for (User user : users) {
            names.add(user.getName());
        }

        nameComboBox.getItems().clear();
        nameComboBox.getItems().addAll(names);
    }

    /**
     * Обробник натискання на кнопку для оновлення даних про користувача.
     */
    public void updateButtonOnAction() {
        String name = nameComboBox.getSelectionModel().getSelectedItem();
        if (name == null) {
            AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Вкажіть ім'я користувача, дані якого бажаєте змінити");
        } else {
            // користувач обраний
            String newName = newNameTextField.getText();
            if (newName.matches("^\\s*$")) {
                AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Нове ім'я не може бути порожнім");
            } else {
                // ім'я відповідає вимогам
                String newPassword = newPasswordTextField.getText();

                if (newPassword.isEmpty()) {
                    AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Новий пароль не вказаний");
                } else {
                    // пароль відповідає вимогам
                    newNameTextField.setText("");
                    newPasswordTextField.setText("");

                    Map<String, Object> response = new HashMap<>();
                    response.put("name", name);
                    response.put("newName", newName);
                    response.put("newPassword", newPassword);

                    primaryStageController.setLastResponse(response);
                    updateUserStage.close();
                }
            }
        }
    }

    /**
     * Відображає вікно для очікування відповіді.
     */
    public void showAndWait() {
        updateNameComboBox();
        updateUserStage.showAndWait();
    }
}
