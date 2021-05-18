package com.fluffy.server;

import com.fluffy.server.daos.impls.FirebirdUserDAO;
import com.fluffy.server.services.ServerService;
import com.fluffy.server.services.impls.UserServiceImpl;
import com.fluffy.server.util.DataSource;
import com.fluffy.util.ApplicationContext;
import com.fluffy.util.Environment;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Головний клас додатку, містить точку входу, ініціалізацію GUI та контексту.
 * @author Сивоконь Вадим
 */
public final class ServerApplication extends Application {
    /**
     * Точка входу в програму.
     * @param args аргументи запуску
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    private void configureStage(final Stage stage,
                                final String viewName,
                                final String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(Environment.getProperty("gui.view-prefix") + viewName + Environment.getProperty("gui.view-suffix")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(false);
        stage.getIcons().add(new Image(Environment.getProperty("gui.application-icon")));
    }

    private void configureModalStage(final Stage stage,
                                     final Stage stageOwner,
                                     final String viewName,
                                     final String title) throws IOException {
        configureStage(stage, viewName, title);
        stage.initOwner(stageOwner);
        stage.initModality(Modality.APPLICATION_MODAL);
    }

    /**
     * Ініціалізує GUI та контекст додатку.
     * @param primaryStage головне вікно програми
     * @throws Exception якщо сталася помилка під час ініціалізації
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        // завантаження налаштувань
        Environment.initialize(getClass().getClassLoader().getResourceAsStream("properties.xml"));

        // ініціалізація контексту додатку
        Stage serverStartupStage = new Stage();
        Stage createUserStage = new Stage();
        Stage updateUserStage = new Stage();
        Stage deleteUserStage = new Stage();
        Alert alert = new Alert(Alert.AlertType.NONE);

        // datasource
        DataSource dataSource = new DataSource(
                Environment.getProperty("data-source.connection-url"),
                Environment.getProperty("data-source.user"),
                Environment.getProperty("data-source.password"),
                Environment.getProperty("data-source.driver-class")
        );
        ApplicationContext.registerObject("dataSource", dataSource);

        // dao
        ApplicationContext.registerObject("userDAO", new FirebirdUserDAO());

        // сервіси
        ApplicationContext.registerObject("serverService", new ServerService());
        ApplicationContext.registerObject("userService", new UserServiceImpl());

        // вікна
        ApplicationContext.registerObject("primaryStage", primaryStage);
        ApplicationContext.registerObject("serverStartupStage", serverStartupStage);

        ApplicationContext.registerObject("createUserStage", createUserStage);
        ApplicationContext.registerObject("updateUserStage", updateUserStage);
        ApplicationContext.registerObject("deleteUserStage", deleteUserStage);
        ApplicationContext.registerObject("alert", alert);

        // головне вікно програми
        configureStage(primaryStage, "ServerApplication", "Сервер");
        primaryStage.show();

        // модальні вікна додатку
        alert.initOwner(primaryStage);
        configureModalStage(serverStartupStage, primaryStage, "ServerStartup", "Запуск сервера");
        configureModalStage(createUserStage, primaryStage, "CreateUser", "Створення користувача");
        configureModalStage(updateUserStage, primaryStage, "UpdateUser", "Оновлення користувача");
        configureModalStage(deleteUserStage, primaryStage, "DeleteUser", "Видалення користувача");
    }
}
