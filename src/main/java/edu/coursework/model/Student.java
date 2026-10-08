package edu.coursework.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "surname", nullable = false, length = 80)
    private String surname;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "patronymic", nullable = false, length = 80)
    private String patronymic;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    public Long getId() {
        return id;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String value) {
        this.surname = value;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String value) {
        this.firstName = value;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String value) {
        this.patronymic = value;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        this.address = value;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String value) {
        this.phone = value;
    }

    @Override
    public String toString() {
        return surname + " " + firstName + " " + patronymic + " (" + phone + ")";
    }
}
