package com.creator.settlement.course.dto;

public record RegisterCourseCommand(
        String courseId,
        String creatorId,
        String title
) {
}
