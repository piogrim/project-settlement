package com.creator.settlement.settlement.service;

import com.creator.settlement.common.exception.ResourceNotFoundException;
import com.creator.settlement.common.time.KstClock;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.settlement.domain.DailySettlementAggregate;
import com.creator.settlement.settlement.repository.DailySettlementAggregateRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DailySettlementAggregateService {

    private final CreatorRepository creatorRepository;
    private final DailySettlementAggregateRepository dailySettlementAggregateRepository;
    private final KstClock kstClock;

    public void addSale(String creatorId, OffsetDateTime paidAt, BigDecimal amount) {
        adjustAggregate(creatorId, toKstDate(paidAt), aggregate -> aggregate.addSale(amount));
    }

    public void addCancellation(String creatorId, OffsetDateTime canceledAt, BigDecimal refundAmount) {
        adjustAggregate(creatorId, toKstDate(canceledAt), aggregate -> aggregate.addCancellation(refundAmount));
    }

    private void adjustAggregate(
            String creatorId,
            LocalDate aggregateDate,
            Consumer<DailySettlementAggregate> mutator
    ) {
        Creator creator = creatorRepository.findByIdForUpdate(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("크리에이터를 찾을 수 없습니다: " + creatorId));

        DailySettlementAggregate aggregate = dailySettlementAggregateRepository
                .findByCreatorIdAndAggregateDate(creatorId, aggregateDate)
                .orElseGet(() -> dailySettlementAggregateRepository.save(
                        DailySettlementAggregate.create(creator, aggregateDate)
                ));

        mutator.accept(aggregate);
    }

    private LocalDate toKstDate(OffsetDateTime occurredAt) {
        return occurredAt.atZoneSameInstant(kstClock.zoneId()).toLocalDate();
    }
}
