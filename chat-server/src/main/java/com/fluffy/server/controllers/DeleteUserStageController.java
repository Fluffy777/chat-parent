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
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.List;

/**
 * Клас контролера модального вікна для видалення користувача.
 * @author Сивоконь Вадим
 */
public class DeleteUserStageController extends AbstractStageController {
    /**
     * Модальне вікно видалення користувача.
     */
    private final Stage deleteUserStage;

    /**
     * Контролер головного вікна.
     */
    private final PrimaryStageController primaryStageController;

    /**
     * Сервіс для отримання даних про користувачів.
     */
    private final UserService userService;

    /**
     * Випадаючий список для обрання імені користувача, що буде видалений.
     */
    @FXML
    private ComboBox<String> nameComboBox;

    /**
     * Конструктор об'єкта контролера.
     */
    public DeleteUserStageController() {
        deleteUserStage = (Stage) ApplicationContext.lookup("deleteUserStage");
        primaryStageController = (PrimaryStageController) ApplicationContext.lookup("primaryStageController");
        userService = (UserService) ApplicationContext.lookup("userService");

        deleteUserStage.setOnCloseRequest(event -> {
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
     * Обробник натискання на кнопку для видалення даних про користувача.
     */
    public void deleteButtonOnAction() {
        String name = nameComboBox.getSelectionModel().getSelectedItem();
        if (name == null) {
            AlertUtil.show(Alert.AlertType.WARNING, "Попередження", "Попередження", "Вкажіть ім'я користувача, дані якого бажаєте видалити");
        } else {
            // ім'я відповідає вимогам
            primaryStageController.setLastResponse(name);
            deleteUserStage.close();
        }
    }

    /**
     * Відображає вікно для очікування відповіді.
     */
    public void showAndWait() {
        updateNameComboBox();
        deleteUserStage.showAndWait();
    }
}
