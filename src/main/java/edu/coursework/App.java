package edu.coursework;

import edu.coursework.controller.LoginController;
import edu.coursework.database.HibernateUtil;
import edu.coursework.service.AuthService;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public final class App extends Application {
    @Override
    public void start(Stage stage) {
        try {
            new AuthService().bootstrapAdmin();
            new LoginController().show(stage);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Ошибка запуска: " + ex.getMessage()).showAndWait();
        }
    }

    @Override
    public void stop() {
        // Если SessionFactory был инициализирован, закрываем при выходе.
        try {
            HibernateUtil.shutdown();
        } catch (Exception ignored) {
        }
    }
}
