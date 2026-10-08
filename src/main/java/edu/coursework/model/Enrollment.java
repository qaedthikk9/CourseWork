package edu.coursework.model;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    @Column(name = "grade")
    private Integer grade;

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student value) {
        this.student = value;
    }

    public Offering getOffering() {
        return offering;
    }

    public void setOffering(Offering value) {
        this.offering = value;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer value) {
        this.grade = value;
    }

    @Override
    public String toString() {
        return student + " / " + offering + " / оценка: " + (grade == null ? "не выставлена" : grade);
    }
}
