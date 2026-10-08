package edu.coursework.model;

import jakarta.persistence.*;

@Entity
@Table(name = "electives")
public class Elective {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Long getId() {
        return id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department value) {
        this.department = value;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject value) {
        this.subject = value;
    }

    @Override
    public String toString() {
        return name + " (" + subject.getName() + ") — " + department.getName();
    }
}
