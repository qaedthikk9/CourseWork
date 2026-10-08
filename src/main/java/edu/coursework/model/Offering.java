package edu.coursework.model;

import jakarta.persistence.*;

@Entity
@Table(name = "offerings")
public class Offering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "elective_id", nullable = false)
    private Elective elective;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "lecture_hours", nullable = false)
    private Integer lectureHours;

    @Column(name = "practice_hours", nullable = false)
    private Integer practiceHours;

    @Column(name = "lab_hours", nullable = false)
    private Integer labHours;

    public Long getId() {
        return id;
    }

    public Elective getElective() {
        return elective;
    }

    public void setElective(Elective value) {
        this.elective = value;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester value) {
        this.semester = value;
    }

    public Integer getLectureHours() {
        return lectureHours;
    }

    public void setLectureHours(Integer value) {
        this.lectureHours = value;
    }

    public Integer getPracticeHours() {
        return practiceHours;
    }

    public void setPracticeHours(Integer value) {
        this.practiceHours = value;
    }

    public Integer getLabHours() {
        return labHours;
    }

    public void setLabHours(Integer value) {
        this.labHours = value;
    }

    @Override
    public String toString() {
        return elective + " / " + semester + " / Л " + lectureHours + ", П " + practiceHours + ", ЛР " + labHours;
    }
}
