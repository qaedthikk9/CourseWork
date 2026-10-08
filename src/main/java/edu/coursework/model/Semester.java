package edu.coursework.model;

import jakarta.persistence.*;

@Entity
@Table(name = "semesters")
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_year", nullable = false)
    private Integer startYear;

    @Column(name = "term_number", nullable = false)
    private Integer termNumber;

    public Long getId() {
        return id;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer value) {
        this.startYear = value;
    }

    public Integer getTermNumber() {
        return termNumber;
    }

    public void setTermNumber(Integer value) {
        this.termNumber = value;
    }

    @Override
    public String toString() {
        return startYear + "/" + (startYear + 1) + ", семестр " + termNumber;
    }
}
