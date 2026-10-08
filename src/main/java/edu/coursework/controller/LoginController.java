package edu.coursework.controller;

import edu.coursework.model.User;
import edu.coursework.service.AuthService;
import edu.coursework.view.LoginView;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public final class LoginController {
    private final AuthService auth = new AuthService();

    public void show(Stage stage) {
        LoginView view = new LoginView();
        view.loginButton.setOnAction(ev -> {
            try {
                User user = auth.login(view.login.getText(), view.password.getText());
                new MainController(stage, user).show();
            } catch (Exception ex) {
                error(ex);
            }
        });
        view.registerButton.setOnAction(ev -> {
            try {
                auth.register(view.newLogin.getText(), view.newPassword.getText(),
                        view.surname.getText(), view.firstName.getText(), view.patronymic.getText(),
                        view.address.getText(), view.phone.getText());
                new Alert(Alert.AlertType.INFORMATION, "Регистрация выполнена. Войдите под новым логином.").showAndWait();
            } catch (Exception ex) {
                error(ex);
            }
        });
        stage.setTitle("Факультативы — вход");
        stage.setScene(new Scene(view.build(), 760, 510));
        stage.show();
    }

    static void error(Exception ex) {
        new Alert(Alert.AlertType.ERROR, ex.getMessage() == null ? "Ошибка работы с БД" : ex.getMessage()).showAndWait();
    }
}
