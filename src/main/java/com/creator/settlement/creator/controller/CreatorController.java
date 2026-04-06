package com.creator.settlement.creator.controller;

import com.creator.settlement.creator.dto.CreateCreatorRequest;
import com.creator.settlement.creator.dto.CreatorResult;
import com.creator.settlement.creator.service.CreatorCommandService;
import jakarta.validation.Valid;
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
public class CreatorController {

    private final CreatorCommandService creatorCommandService;

    @PostMapping("/creators")
    public ResponseEntity<CreatorResult> createCreator(@Valid @RequestBody CreateCreatorRequest request) {
        CreatorResult result = creatorCommandService.createCreator(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
