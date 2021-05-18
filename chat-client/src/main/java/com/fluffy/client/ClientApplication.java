package com.fluffy.client;

import com.fluffy.client.services.ClientService;
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
public class ClientApplication extends Application {
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
        ClientService clientService = new ClientService();
        Stage clientConnectionStage = new Stage();
        Alert alert = new Alert(Alert.AlertType.NONE);

        ApplicationContext.registerObject("clientService", clientService);
        ApplicationContext.registerObject("primaryStage", primaryStage);
        ApplicationContext.registerObject("clientConnectionStage", clientConnectionStage);
        ApplicationContext.registerObject("alert", alert);

        // головне вікно програми
        configureStage(primaryStage, "ClientApplication", "Клієнт");
        primaryStage.show();

        // модальні вікна
        alert.initOwner(primaryStage);
        configureModalStage(clientConnectionStage, primaryStage, "ClientConnection", "Підключення до сервера");
    }
}
