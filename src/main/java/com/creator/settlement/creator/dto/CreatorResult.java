package com.creator.settlement.creator.dto;

import com.creator.settlement.creator.domain.Creator;

public record CreatorResult(
        String creatorId,
        String name
) {

    public static CreatorResult from(Creator creator) {
        return new CreatorResult(creator.getId(), creator.getName());
    }
}
