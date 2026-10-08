package edu.coursework.view;

import edu.coursework.model.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Function;

public final class MainView {
    public final ComboBox<String> section = new ComboBox<>();
    public final TableView<Object> adminTable = new TableView<>();
    public final Button add = new Button("Добавить");
    public final Button edit = new Button("Изменить");
    public final Button delete = new Button("Удалить");
    public final Button refresh = new Button("Обновить");
    public final TableView<Offering> catalog = new TableView<>();
    public final TableView<Enrollment> mine = new TableView<>();
    public final Button enroll = new Button("Записаться");
    public final Button studentRefresh = new Button("Обновить списки");
    public final ComboBox<Subject> gradeSubject = new ComboBox<>();
    public final Button finalGrade = new Button("Итоговая оценка за предмет");
    public final Label progress = new Label();
    public final Button profile = new Button("Мой профиль");
    public final Button logout = new Button("Выйти");

    public Parent build(User user) {
        BorderPane root = new BorderPane();
        HBox header = new HBox(14, new Label("Факультативы кафедры | " + user.getLogin()
                + " | " + user.getRole()), profile, logout);
        header.setPadding(new Insets(12));
        root.setTop(header);
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        if (user.getRole() == Role.ADMIN) {
            section.getItems().addAll("Студенты", "Кафедры", "Предметы", "Факультативы",
                    "Семестры", "Проведения", "Записи и оценки", "Учётные записи");
            section.getSelectionModel().selectFirst();
            // Набор колонок выбирается контроллером после выбора раздела.
            configureAdminColumns(Student.class);
            HBox actions = new HBox(8, section, add, edit, delete, refresh);
            VBox adminPane = new VBox(10, actions, adminTable,
                    new Label("Удаление записей с зависимостями блокируется внешними ключами MySQL."));
            adminPane.setPadding(new Insets(12));
            VBox.setVgrow(adminTable, Priority.ALWAYS);
            tabs.getTabs().add(new Tab("Справочники и данные", adminPane));
        } else {
            configureCatalogColumns();
            configureMineColumns();
            VBox studentPane = new VBox(10, new Label("Доступные факультативы"), catalog,
                    new HBox(8, enroll, studentRefresh), new Label("Мои записи"), mine, progress,
                    new HBox(8, gradeSubject, finalGrade));
            studentPane.setPadding(new Insets(12));
            VBox.setVgrow(catalog, Priority.ALWAYS); VBox.setVgrow(mine, Priority.ALWAYS);
            tabs.getTabs().add(new Tab("Каталог и мои результаты", studentPane));
        }
        root.setCenter(tabs);
        return root;
    }
    /** Разные читаемые колонки вместо одной строки toString(). */
    public void configureAdminColumns(Class<?> type) {
        adminTable.getColumns().clear();
        adminTable.getColumns().add(column("ID", 65, MainView::idOf));
        if (type == Student.class) {
            adminTable.getColumns().addAll(
                    column("Фамилия", 150, x -> ((Student)x).getSurname()),
                    column("Имя", 120, x -> ((Student)x).getFirstName()),
                    column("Отчество", 150, x -> ((Student)x).getPatronymic()),
                    column("Телефон", 165, x -> ((Student)x).getPhone()),
                    column("Адрес", 300, x -> ((Student)x).getAddress()));
        } else if (type == Department.class) {
            adminTable.getColumns().add(column("Кафедра", 400, x -> ((Department)x).getName()));
        } else if (type == Subject.class) {
            adminTable.getColumns().add(column("Предмет", 400, x -> ((Subject)x).getName()));
        } else if (type == Elective.class) {
            adminTable.getColumns().addAll(
                    column("Название факультатива", 260, x -> ((Elective)x).getName()),
                    column("Предмет", 230, x -> ((Elective)x).getSubject().getName()),
                    column("Кафедра", 260, x -> ((Elective)x).getDepartment().getName()));
        } else if (type == Semester.class) {
            adminTable.getColumns().addAll(
                    column("Учебный год", 160, x -> year(((Semester)x).getStartYear())),
                    column("Семестр", 120, x -> str(((Semester)x).getTermNumber())));
        } else if (type == Offering.class) {
            adminTable.getColumns().addAll(
                    column("Факультатив", 270, x -> ((Offering)x).getElective().getName()),
                    column("Кафедра", 220, x -> ((Offering)x).getElective().getDepartment().getName()),
                    column("Учебный год", 140, x -> year(((Offering)x).getSemester().getStartYear())),
                    column("Семестр", 105, x -> str(((Offering)x).getSemester().getTermNumber())),
                    column("Лекции", 95, x -> str(((Offering)x).getLectureHours())),
                    column("Практика", 95, x -> str(((Offering)x).getPracticeHours())),
                    column("Лабораторные", 125, x -> str(((Offering)x).getLabHours())));
        } else if (type == Enrollment.class) {
            adminTable.getColumns().addAll(enrollmentColumns());
        } else if (type == User.class) {
            adminTable.getColumns().addAll(
                    column("Логин", 200, x -> ((User)x).getLogin()),
                    column("Роль", 120, x -> str(((User)x).getRole())),
                    column("Привязанный студент", 320, x -> fullName(((User)x).getStudent())));
        }
    }

    private static TableColumn<Object,String>[] enrollmentColumns() {
        @SuppressWarnings("unchecked")
        TableColumn<Object,String>[] cols = new TableColumn[] {
                column("ФИО студента", 245, x -> fullName(((Enrollment)x).getStudent())),
                column("Телефон", 160, x -> ((Enrollment)x).getStudent().getPhone()),
                column("Факультатив", 240, x -> ((Enrollment)x).getOffering().getElective().getName()),
                column("Предмет", 210, x -> ((Enrollment)x).getOffering().getElective().getSubject().getName()),
                column("Кафедра", 220, x -> ((Enrollment)x).getOffering().getElective().getDepartment().getName()),
                column("Учебный год", 135, x -> year(((Enrollment)x).getOffering().getSemester().getStartYear())),
                column("Семестр", 105, x -> str(((Enrollment)x).getOffering().getSemester().getTermNumber())),
                column("Лекции", 90, x -> str(((Enrollment)x).getOffering().getLectureHours())),
                column("Практика", 90, x -> str(((Enrollment)x).getOffering().getPracticeHours())),
                column("Лабораторные", 120, x -> str(((Enrollment)x).getOffering().getLabHours())),
                column("Оценка", 105, x -> ((Enrollment)x).getGrade() == null ? "Не выставлена" : str(((Enrollment)x).getGrade()))
        };
        return cols;
    }

    private void configureCatalogColumns() {
        catalog.getColumns().clear();
        catalog.getColumns().addAll(
                column("Факультатив", 260, x -> x.getElective().getName()),
                column("Предмет", 235, x -> x.getElective().getSubject().getName()),
                column("Кафедра", 220, x -> x.getElective().getDepartment().getName()),
                column("Учебный год", 140, x -> year(x.getSemester().getStartYear())),
                column("Семестр", 100, x -> str(x.getSemester().getTermNumber())),
                column("Лекции", 95, x -> str(x.getLectureHours())),
                column("Практика", 100, x -> str(x.getPracticeHours())),
                column("Лабораторные", 130, x -> str(x.getLabHours())));
    }

    private void configureMineColumns() {
        mine.getColumns().clear();
        mine.getColumns().addAll(
                column("Факультатив", 275, x -> x.getOffering().getElective().getName()),
                column("Предмет", 240, x -> x.getOffering().getElective().getSubject().getName()),
                column("Учебный год", 140, x -> year(x.getOffering().getSemester().getStartYear())),
                column("Семестр", 110, x -> str(x.getOffering().getSemester().getTermNumber())),
                column("Оценка", 135, x -> x.getGrade() == null ? "Не выставлена" : str(x.getGrade())));
    }

    private static <T> TableColumn<T,String> column(String title, double width, Function<T,String> text) {
        TableColumn<T,String> col = new TableColumn<>(title);
        col.setCellValueFactory(v -> new ReadOnlyStringWrapper(str(text.apply(v.getValue()))));
        col.setPrefWidth(width);
        return col;
    }

    private static String fullName(Student x) {
        if (x == null) return "—";
        return x.getSurname() + " " + x.getFirstName() + " " + x.getPatronymic();
    }
    private static String year(Integer start) {
        return start == null ? "" : start + "/" + (start + 1);
    }
    private static String str(Object val) { return val == null ? "" : val.toString(); }

    private static String idOf(Object entity) {
        if (entity instanceof Student x) return String.valueOf(x.getId());
        if (entity instanceof Department x) return String.valueOf(x.getId());
        if (entity instanceof Subject x) return String.valueOf(x.getId());
        if (entity instanceof Elective x) return String.valueOf(x.getId());
        if (entity instanceof Semester x) return String.valueOf(x.getId());
        if (entity instanceof Offering x) return String.valueOf(x.getId());
        if (entity instanceof Enrollment x) return String.valueOf(x.getId());
        if (entity instanceof User x) return String.valueOf(x.getId());
        return "";
    }
}
