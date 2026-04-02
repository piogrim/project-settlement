package com.creator.settlement.course.dto;

import com.creator.settlement.course.domain.Course;

public record CourseResult(
        String courseId,
        String creatorId,
        String title
) {

    public static CourseResult from(Course course) {
        return new CourseResult(
                course.getId(),
                course.getCreator().getId(),
                course.getTitle()
        );
    }
}
