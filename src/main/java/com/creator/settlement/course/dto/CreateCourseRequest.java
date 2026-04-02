package com.creator.settlement.course.dto;

public record CreateCourseRequest(
        String courseId,
        String creatorId,
        String title
) {

    public RegisterCourseCommand toCommand() {
        return new RegisterCourseCommand(courseId, creatorId, title);
    }
}
