package edu.coursework.repository;

import edu.coursework.database.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CrudRepository<T> {
    private final Class<T> type;

    public CrudRepository(Class<T> type) {
        this.type = type;
    }

    public List<T> all() {
        try (Session session = HibernateUtil.factory().openSession()) {
            // Имя сущности задаёт сам разработчик, НЕ пользователь.
            return session.createQuery("from " + type.getSimpleName() + " e order by e.id", type).list();
        }
    }

    public T find(long id) {
        try (Session session = HibernateUtil.factory().openSession()) {
            return session.get(type, id);
        }
    }

    public T save(T entity) {
        try (Session session = HibernateUtil.factory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T saved = session.merge(entity);
                tx.commit();
                return saved;
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            }
        }
    }

    public void delete(long id) {
        try (Session session = HibernateUtil.factory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T entity = session.get(type, id);
                if (entity == null) throw new IllegalArgumentException("Объект не найден");
                session.remove(entity);
                tx.commit();
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            }
        }
    }
}
