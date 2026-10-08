package edu.coursework.service;

import edu.coursework.database.HibernateUtil;
import edu.coursework.model.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;


public class AuthService {
    // >=8 символов, одна заглавная буква, цифра, специальный символ.
    public static final String PASSWORD_PATTERN = "^(?=.{8,}$)(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).*$";

    public static void validatePassword(String password) {
        if (password == null || !password.matches(PASSWORD_PATTERN)) {
            throw new IllegalArgumentException("Пароль: минимум 8 символов, заглавная буква, цифра и спецсимвол");
        }
    }

    public static void validateLogin(String login) {
        if (login == null || !login.matches("[A-Za-z0-9_]{3,80}"))
            throw new IllegalArgumentException("Логин: от 3 до 80 латинских букв, цифр или _");
    }


    public User login(String login, String password) {
        try (Session session = HibernateUtil.factory().openSession()) {
            User user = session.createQuery("from User u where u.login = :login", User.class)
                    .setParameter("login", login).uniqueResult();
            if (user == null || password == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
                throw new IllegalArgumentException("Неверный логин или пароль");
            }
            return user;
        }
    }


    public void register(String login, String password,
                         String surname, String firstName,
                         String patronymic, String address,
                         String phone) {

        validateLogin(login);
        validatePassword(password);

        if (blank(surname) || blank(firstName) ||
                blank(patronymic) || blank(address) || blank(phone)) {
            throw new IllegalArgumentException(
                    "Все сведения о студенте обязательны");
        }

        login = login.trim();

        try (Session session = HibernateUtil.factory().openSession()) {

            Long count = session.createQuery(
                            "SELECT COUNT(u) FROM User u " +
                                    "WHERE LOWER(u.login) = :login", Long.class)
                    .setParameter("login", login.toLowerCase(
                            java.util.Locale.ROOT))
                    .getSingleResult();

            if (count > 0) {
                throw new IllegalArgumentException(
                        "Пользователь с таким логином уже существует.");
            }

            Transaction tx = session.beginTransaction();

            try {
                Student student = new Student();
                student.setSurname(surname.trim());
                student.setFirstName(firstName.trim());
                student.setPatronymic(patronymic.trim());
                student.setAddress(address.trim());
                student.setPhone(phone.trim());
                session.persist(student);

                User user = new User();
                user.setLogin(login);
                user.setRole(Role.STUDENT);
                user.setStudent(student);
                user.setPasswordHash(
                        BCrypt.hashpw(password, BCrypt.gensalt(12)));
                session.persist(user);

                tx.commit();

            } catch (RuntimeException ex) {
                if (tx.isActive()) {
                    tx.rollback();
                }

                if (isDuplicateLogin(ex)) {
                    throw new IllegalArgumentException(
                            "Пользователь с таким логином уже существует.", ex);
                }

                throw ex;
            }
        }
    }

    private boolean isDuplicateLogin(Throwable error) {
        Throwable cause = error;

        while (cause != null) {
            if (cause instanceof java.sql.SQLException sql
                    && sql.getErrorCode() == 1062) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }


    public User updateProfile(User authenticated, String login, String newPassword,
                              String address, String phone) {
        validateLogin(login);
        if (newPassword != null && !newPassword.isBlank()) validatePassword(newPassword);
        try (Session session = HibernateUtil.factory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                User user = session.get(User.class, authenticated.getId());
                if (user == null) throw new IllegalArgumentException("Учетная запись не найдена");
                user.setLogin(login);
                if (newPassword != null && !newPassword.isBlank())
                    user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
                if (user.getRole() == Role.STUDENT) {
                    if (blank(address) || blank(phone))
                        throw new IllegalArgumentException("Адрес и телефон обязательны");
                    user.getStudent().setAddress(address.trim());
                    user.getStudent().setPhone(phone.trim());
                }
                tx.commit();
                return user;
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            }
        }
    }

    public void bootstrapAdmin() {
        String login = System.getenv("ADMIN_LOGIN");
        String password = System.getenv("ADMIN_PASSWORD");
        if (login == null || password == null) return;
        validateLogin(login);
        validatePassword(password);
        try (Session session = HibernateUtil.factory().openSession()) {
            if (session.createQuery("select count(u.id) from User u where u.role = :role", Long.class)
                    .setParameter("role", Role.ADMIN).getSingleResult() > 0) return;
            Transaction tx = session.beginTransaction();
            try {
                User admin = new User();
                admin.setLogin(login);
                admin.setRole(Role.ADMIN);
                admin.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
                session.persist(admin);
                tx.commit();
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            }
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
