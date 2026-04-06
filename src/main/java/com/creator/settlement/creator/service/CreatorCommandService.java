package com.creator.settlement.creator.service;

import com.creator.settlement.common.exception.BusinessRuleViolationException;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.dto.CreatorResult;
import com.creator.settlement.creator.dto.RegisterCreatorCommand;
import com.creator.settlement.creator.repository.CreatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatorCommandService {

    private final CreatorRepository creatorRepository;

    public CreatorResult createCreator(RegisterCreatorCommand command) {
        if (creatorRepository.existsById(command.creatorId())) {
            throw new BusinessRuleViolationException("이미 존재하는 크리에이터 ID입니다: " + command.creatorId());
        }

        Creator creator = creatorRepository.save(Creator.builder()
                .id(command.creatorId())
                .name(command.name())
                .build());

        return CreatorResult.from(creator);
    }
}
