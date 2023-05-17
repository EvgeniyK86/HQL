package by.itacademy.hibernate.dao;


import by.itacademy.hibernate.dto.PaymentFilter;
import by.itacademy.hibernate.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static by.itacademy.hibernate.entity.QCompany.company;
import static by.itacademy.hibernate.entity.QPayment.payment;
import static by.itacademy.hibernate.entity.QUser.user;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDao {

    private static final UserDao INSTANCE = new UserDao();

    /**
     * Возвращает всех сотрудников
     */
    public List<User> findAll(Session session) {

        return session.createQuery("select u from User u", User.class).list();

    }

    /**
     * Возвращает всех сотрудников с указанным именем
     */
    public List<User> findAllByFirstName(Session session, String firstName) {
        return session.createQuery("select u from User u" +
                        " where u.personalInfo.firstname=:firstname", User.class)
                .setParameter("firstname", firstName).list();
    }

    /**
     * Возвращает первые {limit} сотрудников, упорядоченных по дате рождения (в порядке возрастания)
     */
    public List<User> findLimitedUsersOrderedByBirthday(Session session, int limit) {

        return session.createQuery("select u from User u" +
                        " order by (u.personalInfo.birthDate) ", User.class)
                .setMaxResults(limit).list();
    }

    /**
     * Возвращает всех сотрудников компании с указанным названием
     */
    public List<User> findAllByCompanyName(Session session, String companyName) {
        return session.createQuery("select u from User u" +
                        " where u.company.name=:company", User.class)
                .setParameter("company", companyName)
                .list();
    }

    /**
     * Возвращает все выплаты, полученные сотрудниками компании с указанными именем,
     * упорядоченные по имени сотрудника, а затем по размеру выплаты
     */
    public List<Payment> findAllPaymentsByCompanyName(Session session, String companyName) {

        return session.createQuery("select p from Payment p " +
                        "join User u on u.id = p.receiver.id " +
                        "where u.company.name=:company " +
                        "order by u.personalInfo.firstname, " +
                        "p.amount", Payment.class)
                .setParameter("company", companyName)
                .list();

    }

    /**
     * Возвращает среднюю зарплату сотрудника с указанными именем и фамилией
     */
    public Double findAveragePaymentAmountByFirstAndLastNames(Session session, PaymentFilter filter) {

        List<Double> list = session.createQuery("select avg (p.amount) from Payment p" +
                        " join User u on u.id = p.receiver.id " +
                        "group by u.id " +
                        "having u.personalInfo.firstname=:firstname " +
                        "and u.personalInfo.lastname=:lastname", Double.class)
                .setParameter("firstname", filter.getFirstname())
                .setParameter("lastname", filter.getLastname())
                .list();
        return list.get(1);
    }

    /**
     * Возвращает для каждой компании: название, среднюю зарплату всех её сотрудников. Компании упорядочены по названию.
     */
    public List<Tuple> findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(Session session) {
        return session.createQuery("select c.name, " +
                "avg (p.amount) from Company c " +
                "join  User u on c.id = u.company.id " +
                "join Payment p on u.id = p.receiver.id " +
                "group by c.name " +
                "order by c.name", Tuple.class).list();
    }

    /**
     * Возвращает список: сотрудник (объект User), средний размер выплат, но только для тех сотрудников, чей средний размер выплат
     * больше среднего размера выплат всех сотрудников
     * Упорядочить по имени сотрудника
     */
    public List<Tuple> isItPossible(Session session) {
        return session.createQuery("select u, avg(p.amount) from User u " +
                "join Payment  p on u.id = p.receiver.id " +
                "group by u.id " +
                "having (avg(p.amount))>(select avg(p.amoumt) from Payment p) " +
                "order by u.personalInfo.firstname", Tuple.class).list();
    }

    public static UserDao getInstance() {
        return INSTANCE;
    }
}