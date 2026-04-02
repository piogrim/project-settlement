package com.creator.settlement.creator.dto;

public record CreateCreatorRequest(
        String creatorId,
        String name
) {

    public RegisterCreatorCommand toCommand() {
        return new RegisterCreatorCommand(creatorId, name);
    }
}
