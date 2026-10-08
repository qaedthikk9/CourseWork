package edu.coursework.service;

import edu.coursework.database.HibernateUtil;
import edu.coursework.model.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ElectiveService {
    // Учебный пример норматива; изменить в одном месте при уточнении кафедрой.
    public static final int MIN_REQUIRED_HOURS = 72;

    public void enroll(User user, long offeringId) {
        if (user.getRole() != Role.STUDENT || user.getStudent() == null)
            throw new IllegalArgumentException("Запись доступна только студенту");
        try (Session session = HibernateUtil.factory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Offering offering = session.get(Offering.class, offeringId);
                if (offering == null) throw new IllegalArgumentException("Факультатив не найден");
                Long n = session.createQuery("select count(e.id) from Enrollment e " +
                                "where e.student.id = :student and e.offering.id = :offering", Long.class)
                        .setParameter("student", user.getStudent().getId())
                        .setParameter("offering", offeringId).getSingleResult();
                if (n > 0) throw new IllegalArgumentException("Вы уже записаны");
                Enrollment en = new Enrollment();
                en.setStudent(session.get(Student.class, user.getStudent().getId()));
                en.setOffering(offering);
                en.setGrade(null);
                session.persist(en);
                tx.commit();
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            }
        }
    }

    public List<Enrollment> myEnrollments(User user) {
        if (user.getRole() != Role.STUDENT || user.getStudent() == null)
            throw new IllegalArgumentException("Нет доступа к чужим результатам");
        try (Session session = HibernateUtil.factory().openSession()) {
            return session.createQuery("from Enrollment e where e.student.id = :student order by e.id", Enrollment.class)
                    .setParameter("student", user.getStudent().getId()).list();
        }
    }

    public int completedHours(User user) {
        return myEnrollments(user).stream().filter(e -> e.getGrade() != null)
                .mapToInt(e -> e.getOffering().getLectureHours() + e.getOffering().getPracticeHours()
                        + e.getOffering().getLabHours()).sum();
    }

    // Последняя по хронологии семестра оценка за предмет, а не по id записи.
    public Integer finalGrade(User user, long subjectId) {
        if (user.getRole() != Role.STUDENT) throw new IllegalArgumentException("Нет доступа");
        try (Session session = HibernateUtil.factory().openSession()) {
            return session.createQuery("select en.grade from Enrollment en join en.offering o " +
                            "join o.semester sm join o.elective el where en.student.id = :student " +
                            "and el.subject.id = :subject and en.grade is not null " +
                            "order by sm.startYear desc, sm.termNumber desc", Integer.class)
                    .setParameter("student", user.getStudent().getId())
                    .setParameter("subject", subjectId).setMaxResults(1)
                    .uniqueResult();
        }
    }
}
