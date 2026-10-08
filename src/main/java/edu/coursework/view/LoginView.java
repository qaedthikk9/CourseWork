package edu.coursework.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public final class LoginView {
    public final TextField login = new TextField();
    public final PasswordField password = new PasswordField();
    public final Button loginButton = new Button("Войти");
    public final TextField newLogin = new TextField();
    public final PasswordField newPassword = new PasswordField();
    public final TextField surname = new TextField();
    public final TextField firstName = new TextField();
    public final TextField patronymic = new TextField();
    public final TextField address = new TextField();
    public final TextField phone = new TextField();
    public final Button registerButton = new Button("Зарегистрироваться как студент");

    public Parent build() {
        GridPane enter = grid();
        row(enter, 0, "Логин", login);
        row(enter, 1, "Пароль", password);
        enter.add(loginButton, 1, 2);

        GridPane register = grid();
        row(register, 0, "Логин", newLogin);
        row(register, 1, "Пароль", newPassword);
        row(register, 2, "Фамилия", surname);
        row(register, 3, "Имя", firstName);
        row(register, 4, "Отчество", patronymic);
        row(register, 5, "Адрес", address);
        row(register, 6, "Телефон", phone);
        register.add(registerButton, 1, 7);
        Label hint = new Label("Пароль: 8+ символов, заглавная буква, цифра, спецсимвол");
        VBox registrationPane = new VBox(12, register, hint);
        TabPane tabs = new TabPane(new Tab("Вход", enter), new Tab("Регистрация", registrationPane));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox root = new VBox(12, new Label("Факультативы кафедры — вариант 9"), tabs);
        root.setPadding(new Insets(20));
        return root;
    }
    private static GridPane grid() { GridPane g = new GridPane(); g.setHgap(10); g.setVgap(9); return g; }
    private static void row(GridPane g, int r, String title, Control control) {
        g.add(new Label(title), 0, r); g.add(control, 1, r);
    }
}
