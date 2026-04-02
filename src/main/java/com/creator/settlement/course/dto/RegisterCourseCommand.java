package com.creator.settlement.course.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterCourseCommand(
        @NotBlank(message = "강의 ID는 필수입니다.") String courseId,
        @NotBlank(message = "크리에이터 ID는 필수입니다.") String creatorId,
        @NotBlank(message = "강의 제목은 필수입니다.") String title
) {
}
