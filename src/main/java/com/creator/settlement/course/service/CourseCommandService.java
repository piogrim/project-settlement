package com.creator.settlement.course.service;

import com.creator.settlement.common.exception.BusinessRuleViolationException;
import com.creator.settlement.common.exception.ResourceNotFoundException;
import com.creator.settlement.course.domain.Course;
import com.creator.settlement.course.dto.CourseResult;
import com.creator.settlement.course.dto.RegisterCourseCommand;
import com.creator.settlement.course.repository.CourseRepository;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class CourseCommandService {

    private final CourseRepository courseRepository;
    private final CreatorRepository creatorRepository;

    public CourseResult registerCourse(@NotNull @Valid RegisterCourseCommand command) {
        if (courseRepository.existsById(command.courseId())) {
            throw new BusinessRuleViolationException("이미 존재하는 강의 ID입니다: " + command.courseId());
        }

        Creator creator = creatorRepository.findById(command.creatorId())
                .orElseThrow(() -> new ResourceNotFoundException("크리에이터를 찾을 수 없습니다: " + command.creatorId()));

        Course course = courseRepository.save(Course.builder()
                .id(command.courseId())
                .creator(creator)
                .title(command.title())
                .build());

        return CourseResult.from(course);
    }
}
