package com.vulpex.silene;
import java.util.Date;

/**
 * Defines a Lecture
 */
public class Lecture {
    private final String tutorName;
    private final String studentName;
    private final Date scheduledDate;
    private LectureState state;
    private final int lectureId;


    /**
     * Initialise a Lecture. This is private to package level.
     * @param tutorName Name of the tutor.
     * @param studentName Name of the student.
     * @param state State of the lecture.
     * @param scheduledDate the Date of the lecture.
     */
    Lecture (int lectureId, String tutorName, String studentName, LectureState state, Date scheduledDate) {
        this.lectureId = lectureId;
        this.tutorName = tutorName;
        this.studentName = studentName;
        this.scheduledDate  = scheduledDate;
        this.state = state;
    }

    protected void confirmLecture() {
        this.state = LectureState.CONFIRMED;
    }

    public int getLectureId() { return this.lectureId; }
}
