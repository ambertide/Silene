package com.vulpex.silene;


/**
 * Defines the state of a Lecture.
 * A REQUESTED lecture is requested from a Tutor by a Student.
 * A CONFIRMED lecture is confirmed by a Tutor and a Student.
 */
public enum LectureState {
    REQUESTED,
    CONFIRMED
}
