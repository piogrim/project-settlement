package com.creator.settlement.course.controller;

import com.creator.settlement.course.dto.CourseResult;
import com.creator.settlement.course.dto.CreateCourseRequest;
import com.creator.settlement.course.service.CourseCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CourseController {

    private final CourseCommandService courseCommandService;

    @PostMapping("/courses")
    public ResponseEntity<CourseResult> registerCourse(@RequestBody CreateCourseRequest request) {
        CourseResult result = courseCommandService.registerCourse(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
