package com.fluffy.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.LinkedList;
import java.util.Optional;

/**
 * Допоміжний клас для відображення повідомлення в окремому вікні.
 * @author Сивоконь Вадим
 */
public final class AlertUtil {
    /**
     * Тип кнопки "ОК".
     */
    public static final ButtonType OK_BUTTON;

    /**
     * Тип кнопки "Відмінити".
     */
    public static final ButtonType CANCEL_BUTTON;

    private AlertUtil() { }

    static {
        // локалізація кнопок
        OK_BUTTON = new ButtonType("ОК", ButtonBar.ButtonData.OK_DONE);
        CANCEL_BUTTON = new ButtonType("Відмінити", ButtonBar.ButtonData.CANCEL_CLOSE);
    }

    /**
     * Відображає модальне вікно з повідомленням.
     * @param alertType тип повідомлення
     * @param title заголовок вікна
     * @param headerText заголовок повідомлення
     * @param contextText текст повідомлення
     * @return тип кнопки, що була натиснута
     */
    public static Optional<ButtonType> show(final Alert.AlertType alertType,
                                            final String title,
                                            final String headerText,
                                            final String contextText) {
        Alert alert = (Alert) ApplicationContext.lookup("alert");
        alert.setAlertType(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);

        switch (alertType) {
            default:
            case ERROR:
            case WARNING:
            case INFORMATION:
                alert.getButtonTypes().setAll(OK_BUTTON);
                break;
            case CONFIRMATION:
                alert.getButtonTypes().setAll(OK_BUTTON, CANCEL_BUTTON);
                break;
            case NONE:
                alert.getButtonTypes().setAll(new LinkedList<>());
                break;
        }
        return alert.showAndWait();
    }
}
