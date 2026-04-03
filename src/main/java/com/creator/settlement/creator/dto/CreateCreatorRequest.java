package com.creator.settlement.creator.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCreatorRequest(
        @NotBlank(message = "크리에이터 ID는 필수입니다.") String creatorId,
        @NotBlank(message = "크리에이터 이름은 필수입니다.") String name
) {

    public RegisterCreatorCommand toCommand() {
        return new RegisterCreatorCommand(creatorId, name);
    }
}
