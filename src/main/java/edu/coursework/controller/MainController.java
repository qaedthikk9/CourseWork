package edu.coursework.controller;

import edu.coursework.model.*;
import edu.coursework.repository.CrudRepository;
import edu.coursework.service.AuthService;
import edu.coursework.service.ElectiveService;
import edu.coursework.service.UserAdminService;
import edu.coursework.view.MainView;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public final class MainController {
    private final Stage stage;
    private User user;
    private final MainView view = new MainView();
    private final ElectiveService electives = new ElectiveService();
    private final AuthService auth = new AuthService();
    private final UserAdminService userAdmin = new UserAdminService();

    public MainController(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        var root = view.build(user);
        view.profile.setOnAction(ev -> guard(this::profile));
        view.logout.setOnAction(ev -> new LoginController().show(stage));
        if (user.getRole() == Role.ADMIN) {
            view.section.setOnAction(ev -> guard(this::refreshAdmin));
            view.refresh.setOnAction(ev -> guard(this::refreshAdmin));
            view.add.setOnAction(ev -> guard(() -> editEntity(null)));
            view.edit.setOnAction(ev -> guard(() -> {
                Object row = view.adminTable.getSelectionModel().getSelectedItem();
                if (row == null) throw new IllegalArgumentException("Сначала выберите строку");
                editEntity(row);
            }));
            view.delete.setOnAction(ev -> guard(this::deleteEntity));
            refreshAdmin();
        } else {
            view.enroll.setOnAction(ev -> guard(() -> {
                Offering selected = view.catalog.getSelectionModel().getSelectedItem();
                if (selected == null) throw new IllegalArgumentException("Выберите проведение факультатива");
                electives.enroll(user, selected.getId());
                refreshStudent();
            }));
            view.studentRefresh.setOnAction(ev -> guard(this::refreshStudent));
            view.finalGrade.setOnAction(ev -> guard(() -> {
                Subject subject = view.gradeSubject.getValue();
                if (subject == null) throw new IllegalArgumentException("Выберите предмет");
                Integer grade = electives.finalGrade(user, subject.getId());
                info("Итоговая оценка: " + (grade == null ? "не получена" : grade));
            }));
            refreshStudent();
        }
        stage.setTitle("Вариант 9 — факультативы кафедры");
        stage.setScene(new Scene(root, 1140, 720));
        stage.show();
    }

    private void guard(Runnable action) {
        try {
            action.run();
        } catch (SecurityException ex) {
            LoginController.error(ex);
            new LoginController().show(stage);
        } catch (Exception ex) {
            LoginController.error(ex);
        }
    }

    private void adminOnly() {
        userAdmin.requireAdmin(user.getId());
    }

    private void refreshAdmin() {
        adminOnly();
        view.configureAdminColumns(type());
        view.adminTable.setItems(FXCollections.observableArrayList(repositoryData(type())));
        view.add.setDisable(false);
        view.edit.setDisable(false);
    }

    private void refreshStudent() {
        view.catalog.setItems(FXCollections.observableArrayList(new CrudRepository<>(Offering.class).all()));
        view.mine.setItems(FXCollections.observableArrayList(electives.myEnrollments(user)));
        view.gradeSubject.setItems(FXCollections.observableArrayList(new CrudRepository<>(Subject.class).all()));
        int hours = electives.completedHours(user);
        view.progress.setText("Завершено часов: " + hours + " из " + ElectiveService.MIN_REQUIRED_HOURS
                + (hours >= ElectiveService.MIN_REQUIRED_HOURS ? " — норматив выполнен" : " — норматив ещё не выполнен"));
    }

    private Class<?> type() {
        return switch (view.section.getValue()) {
            case "Студенты" -> Student.class;
            case "Кафедры" -> Department.class;
            case "Предметы" -> Subject.class;
            case "Факультативы" -> Elective.class;
            case "Семестры" -> Semester.class;
            case "Проведения" -> Offering.class;
            case "Записи и оценки" -> Enrollment.class;
            case "Учётные записи" -> User.class;
            default -> throw new IllegalArgumentException("Неизвестный раздел");
        };
    }

    private List<?> repositoryData(Class<?> cls) {
        return new CrudRepository<>(cls).all();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void saveEntity(Object entity) {
        new CrudRepository(entity.getClass()).save(entity);
    }

    private void editEntity(Object old) {
        adminOnly();
        if (type() == User.class) {
            editAccount((User) old);
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle((old == null ? "Добавить: " : "Изменить: ") + view.section.getValue());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(14));
        grid.setHgap(12);
        grid.setVgap(10);
        EditForm form = buildForm(old, grid);
        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> answer = dialog.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            form.apply().run();
            saveEntity(form.entity());
            refreshAdmin();
        }
    }

    /**
     * Обычный диалог CRUD для пользователей, без показа BCrypt-хеша.
     */
    private void editAccount(User old) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(old == null ? "Добавить учётную запись" : "Изменить учётную запись");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(14));
        grid.setHgap(12);
        grid.setVgap(10);

        TextField login = field(grid, 0, "Логин", old == null ? "" : old.getLogin());
        PasswordField pass = new PasswordField();
        pass.setPrefColumnCount(28);
        grid.add(new Label(old == null ? "Пароль" : "Новый пароль (необязательно)"), 0, 1);
        grid.add(pass, 1, 1);

        ComboBox<Role> role = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        role.setPrefWidth(460);
        role.setValue(old == null ? Role.STUDENT : old.getRole());
        grid.add(new Label("Роль"), 0, 2);
        grid.add(role, 1, 2);

        ComboBox<Student> student = choice(grid, 3, "Студент (для STUDENT)",
                Student.class, old == null ? null : old.getStudent());
        student.setPromptText("Выберите студента из таблицы «Студенты»");
        student.setDisable(role.getValue() == Role.ADMIN);
        role.setOnAction(ev -> {
            boolean admin = role.getValue() == Role.ADMIN;
            student.setDisable(admin);
            if (admin) student.setValue(null);
        });
        grid.add(new Label(old == null
                ? "Пароль обязателен при создании пользователя."
                : "Пустой пароль оставит старый пароль без изменений."), 0, 4, 2, 1);
        dialog.getDialogPane().setContent(grid);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Long studentId = role.getValue() == Role.STUDENT && student.getValue() != null
                    ? student.getValue().getId() : null;
            if (old == null) {
                userAdmin.create(user.getId(), required(login), pass.getText(), role.getValue(), studentId);
            } else {
                userAdmin.update(user.getId(), old.getId(), required(login),
                        pass.getText(), role.getValue(), studentId);
            }
            refreshAdmin();
            if (old != null && old.getId().equals(user.getId())) {
                info("Ваш профиль обновлён. Войдите заново, чтобы обновить данные сеанса.");
                new LoginController().show(stage);
            }
        }
    }

    private record EditForm(Object entity, Runnable apply) {
    }

    private EditForm buildForm(Object old, GridPane grid) {
        return switch (view.section.getValue()) {
            case "Студенты" -> {
                Student x = old == null ? new Student() : (Student) old;
                TextField a = field(grid, 0, "Фамилия", x.getSurname());
                TextField b = field(grid, 1, "Имя", x.getFirstName());
                TextField c = field(grid, 2, "Отчество", x.getPatronymic());
                TextField d = field(grid, 3, "Адрес", x.getAddress());
                TextField e = field(grid, 4, "Телефон", x.getPhone());
                yield new EditForm(x, () -> {
                    x.setSurname(required(a));
                    x.setFirstName(required(b));
                    x.setPatronymic(required(c));
                    x.setAddress(required(d));
                    x.setPhone(required(e));
                });
            }
            case "Кафедры" -> {
                Department x = old == null ? new Department() : (Department) old;
                TextField name = field(grid, 0, "Название кафедры", x.getName());
                yield new EditForm(x, () -> x.setName(required(name)));
            }
            case "Предметы" -> {
                Subject x = old == null ? new Subject() : (Subject) old;
                TextField name = field(grid, 0, "Название предмета", x.getName());
                yield new EditForm(x, () -> x.setName(required(name)));
            }
            case "Факультативы" -> {
                Elective x = old == null ? new Elective() : (Elective) old;
                TextField name = field(grid, 0, "Название факультатива", x.getName());
                ComboBox<Department> dep = choice(grid, 1, "Кафедра", Department.class, x.getDepartment());
                ComboBox<Subject> sub = choice(grid, 2, "Предмет", Subject.class, x.getSubject());
                yield new EditForm(x, () -> {
                    x.setName(required(name));
                    if (x.getName().length() > 160)
                        throw new IllegalArgumentException("Название факультатива: максимум 160 символов");
                    x.setDepartment(selected(dep));
                    x.setSubject(selected(sub));
                });
            }
            case "Семестры" -> {
                Semester x = old == null ? new Semester() : (Semester) old;
                TextField year = field(grid, 0, "Начало учебного года", value(x.getStartYear()));
                TextField num = field(grid, 1, "Номер семестра (1/2)", value(x.getTermNumber()));
                yield new EditForm(x, () -> {
                    x.setStartYear(number(year, 2000, 2100));
                    x.setTermNumber(number(num, 1, 2));
                });
            }
            case "Проведения" -> {
                Offering x = old == null ? new Offering() : (Offering) old;
                ComboBox<Elective> ele = choice(grid, 0, "Факультатив", Elective.class, x.getElective());
                ComboBox<Semester> sem = choice(grid, 1, "Семестр", Semester.class, x.getSemester());
                TextField lec = field(grid, 2, "Лекции, часы", value(x.getLectureHours()));
                TextField pra = field(grid, 3, "Практики, часы", value(x.getPracticeHours()));
                TextField lab = field(grid, 4, "Лабораторные, часы", value(x.getLabHours()));
                yield new EditForm(x, () -> {
                    x.setElective(selected(ele));
                    x.setSemester(selected(sem));
                    x.setLectureHours(number(lec, 0, 65535));
                    x.setPracticeHours(number(pra, 0, 65535));
                    x.setLabHours(number(lab, 0, 65535));
                    if (x.getLectureHours() + x.getPracticeHours() + x.getLabHours() == 0)
                        throw new IllegalArgumentException("Сумма часов должна быть положительной");
                });
            }
            case "Записи и оценки" -> {
                Enrollment x = old == null ? new Enrollment() : (Enrollment) old;
                ComboBox<Student> st = choice(grid, 0, "Студент", Student.class, x.getStudent());
                ComboBox<Offering> off = choice(grid, 1, "Проведение", Offering.class, x.getOffering());
                TextField grade = field(grid, 2, "Оценка (пусто, 2–5)", value(x.getGrade()));
                yield new EditForm(x, () -> {
                    x.setStudent(selected(st));
                    x.setOffering(selected(off));
                    x.setGrade(grade.getText().isBlank() ? null : number(grade, 2, 5));
                });
            }
            default -> throw new IllegalArgumentException("Не редактируемый раздел");
        };
    }

    private void deleteEntity() {
        adminOnly();
        Object row = view.adminTable.getSelectionModel().getSelectedItem();
        if (row == null) throw new IllegalArgumentException("Сначала выберите строку");
        if (row instanceof User selected && selected.getId().equals(user.getId()))
            throw new IllegalArgumentException("Нельзя удалить собственную учётную запись");
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить выбранную запись?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        long id = idOf(row);
        if (row instanceof User) {
            userAdmin.delete(user.getId(), id);
        } else {
            deleteFromRepository(type(), id);
        }
        refreshAdmin();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void deleteFromRepository(Class<?> cls, long id) {
        new CrudRepository(cls).delete(id);
    }

    private static long idOf(Object x) {
        if (x instanceof Student v) return v.getId();
        if (x instanceof Department v) return v.getId();
        if (x instanceof Subject v) return v.getId();
        if (x instanceof Elective v) return v.getId();
        if (x instanceof Semester v) return v.getId();
        if (x instanceof Offering v) return v.getId();
        if (x instanceof Enrollment v) return v.getId();
        if (x instanceof User v) return v.getId();
        throw new IllegalArgumentException("Неизвестный тип");
    }

    private void profile() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Мой профиль");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(12));
        grid.setVgap(10);
        grid.setHgap(10);
        TextField login = field(grid, 0, "Логин", user.getLogin());
        PasswordField pass = new PasswordField();
        grid.add(new Label("Новый пароль (необязательно)"), 0, 1);
        grid.add(pass, 1, 1);
        Student student = user.getStudent();
        String address = student == null ? "" : student.getAddress();
        String phone = student == null ? "" : student.getPhone();
        if (student != null) grid.add(new Label("Студент: " + student), 0, 2, 2, 1);
        TextField addr = field(grid, 3, "Адрес", address);
        TextField ph = field(grid, 4, "Телефон", phone);
        if (student == null) {
            addr.setDisable(true);
            ph.setDisable(true);
        }
        dialog.getDialogPane().setContent(grid);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            user = auth.updateProfile(user, required(login), pass.getText(), addr.getText(), ph.getText());
            info("Изменения сохранены. При следующем входе используйте новый логин/пароль.");
            new MainController(stage, user).show();
        }
    }

    private static TextField field(GridPane grid, int row, String title, String initial) {
        TextField f = new TextField(initial == null ? "" : initial);
        f.setPrefColumnCount(28);
        grid.add(new Label(title), 0, row);
        grid.add(f, 1, row);
        return f;
    }

    private static <T> ComboBox<T> choice(GridPane grid, int row, String title, Class<T> type, T current) {
        ComboBox<T> c = new ComboBox<>();
        c.setItems(FXCollections.observableArrayList(new CrudRepository<>(type).all()));
        c.setPrefWidth(460);
        if (current != null) {
            for (T item : c.getItems()) {
                if (idOf(item) == idOf(current)) {
                    c.setValue(item);
                    break;
                }
            }
        }
        grid.add(new Label(title), 0, row);
        grid.add(c, 1, row);
        return c;
    }

    private static <T> T selected(ComboBox<T> c) {
        if (c.getValue() == null) throw new IllegalArgumentException("Выберите значение: " + c.getPromptText());
        return c.getValue();
    }

    private static String required(TextField f) {
        if (f.getText() == null || f.getText().isBlank())
            throw new IllegalArgumentException("Заполните все обязательные поля");
        return f.getText().trim();
    }

    private static String value(Object x) {
        return x == null ? "" : x.toString();
    }

    private static int number(TextField f, int min, int max) {
        try {
            int n = Integer.parseInt(required(f));
            if (n < min || n > max) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Нужно целое число от " + min + " до " + max);
        }
    }

    private static void info(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}
