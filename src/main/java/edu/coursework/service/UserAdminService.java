package edu.coursework.service;

import edu.coursework.database.HibernateUtil;
import edu.coursework.model.Role;
import edu.coursework.model.Student;
import edu.coursework.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Административное управление учётными записями. Права проверяются в БД.
 */
public final class UserAdminService {

    /**
     * Проверяем актуальную роль, а не сохранённую при входе копию объекта.
     */
    public void requireAdmin(long actingUserId) {
        try (Session session = HibernateUtil.factory().openSession()) {
            checkAdmin(session, actingUserId);
        }
    }

    private static void checkAdmin(Session session, long actingUserId) {
        User actor = session.get(User.class, actingUserId);
        if (actor == null || actor.getRole() != Role.ADMIN) {
            throw new SecurityException("Нет прав администратора. Войдите заново.");
        }
    }

    public void create(long actingUserId, String login, String password,
                       Role role, Long studentId) {
        AuthService.validateLogin(login);
        AuthService.validatePassword(password);
        try (Session session = HibernateUtil.factory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                checkAdmin(session, actingUserId);
                User created = new User();
                created.setLogin(login);
                created.setRole(role);
                created.setStudent(studentForRole(session, role, studentId, null));
                created.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
                session.persist(created);
                tx.commit();
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            }
        }
    }

    public void update(long actingUserId, long targetId, String login,
                       String newPassword, Role role, Long studentId) {
        AuthService.validateLogin(login);
        if (newPassword != null && !newPassword.isBlank()) {
            AuthService.validatePassword(newPassword);
        }
        try (Session session = HibernateUtil.factory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                checkAdmin(session, actingUserId);
                User target = session.get(User.class, targetId);
                if (target == null) throw new IllegalArgumentException("Учётная запись не найдена");
                if (actingUserId == targetId && role != Role.ADMIN) {
                    throw new IllegalArgumentException("Нельзя снять роль ADMIN с самого себя");
                }
                if (target.getRole() == Role.ADMIN && role != Role.ADMIN) {
                    ensureAnotherAdmin(session, targetId);
                }
                // ВАЖНО: сначала выполняем HQL-проверку студента, и только потом
                // меняем управляемую сущность. Иначе AUTO-flush перед SELECT может
                // сохранить role='STUDENT' при student_id=NULL и нарушить CHECK.
                Student chosenStudent = studentForRole(session, role, studentId, targetId);
                target.setLogin(login);
                target.setStudent(chosenStudent);
                target.setRole(role);
                if (newPassword != null && !newPassword.isBlank()) {
                    target.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
                }
                tx.commit();
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            }
        }
    }

    public void delete(long actingUserId, long targetId) {
        try (Session session = HibernateUtil.factory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                checkAdmin(session, actingUserId);
                if (actingUserId == targetId) {
                    throw new IllegalArgumentException("Нельзя удалить собственную учётную запись");
                }
                User target = session.get(User.class, targetId);
                if (target == null) throw new IllegalArgumentException("Учётная запись не найдена");
                if (target.getRole() == Role.ADMIN) ensureAnotherAdmin(session, targetId);
                session.remove(target);
                tx.commit();
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            }
        }
    }

    private static void ensureAnotherAdmin(Session session, long excludedUserId) {
        Long remaining = session.createQuery(
                        "select count(u.id) from User u where u.role = :role and u.id <> :id", Long.class)
                .setParameter("role", Role.ADMIN)
                .setParameter("id", excludedUserId)
                .getSingleResult();
        if (remaining == 0) throw new IllegalArgumentException("Должен остаться хотя бы один администратор");
    }

    private static Student studentForRole(Session session, Role role,
                                          Long studentId, Long currentUserId) {
        if (role == null) throw new IllegalArgumentException("Выберите роль");
        if (role == Role.ADMIN) {
            if (studentId != null) throw new IllegalArgumentException("Администратор не привязывается к студенту");
            return null;
        }
        if (studentId == null) throw new IllegalArgumentException("Для роли STUDENT выберите студента");
        Student student = session.get(Student.class, studentId);
        if (student == null) throw new IllegalArgumentException("Студент не найден");
        Long occupied = session.createQuery(
                        "select count(u.id) from User u where u.student.id = :studentId "
                                + "and u.id <> :currentId", Long.class)
                .setParameter("studentId", studentId)
                .setParameter("currentId", currentUserId == null ? -1L : currentUserId)
                .getSingleResult();
        if (occupied != 0) throw new IllegalArgumentException("У этого студента уже есть учётная запись");
        return student;
    }
}