package com.creator.settlement.settlement.service;

import com.creator.settlement.common.exception.ResourceNotFoundException;
import com.creator.settlement.common.time.KstClock;
import com.creator.settlement.creator.domain.Creator;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.settlement.domain.DailySettlement;
import com.creator.settlement.settlement.repository.DailySettlementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DailySettlementCommandService {

    private final CreatorRepository creatorRepository;
    private final DailySettlementRepository dailySettlementRepository;
    private final KstClock kstClock;

    public void addSale(String creatorId, OffsetDateTime paidAt, BigDecimal amount) {
        DailySettlement dailySettlement = getOrCreateDailySettlement(creatorId, toKstDate(paidAt));
        dailySettlement.addSale(amount);
    }

    public void addCancellation(String creatorId, OffsetDateTime canceledAt, BigDecimal refundAmount) {
        DailySettlement dailySettlement = getOrCreateDailySettlement(creatorId, toKstDate(canceledAt));
        dailySettlement.addCancellation(refundAmount);
    }

    private DailySettlement getOrCreateDailySettlement(String creatorId, LocalDate settlementDate) {
        Creator creator = creatorRepository.findByIdForUpdate(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("크리에이터를 찾을 수 없습니다: " + creatorId));

        return dailySettlementRepository
                .findByCreatorIdAndSettlementDate(creatorId, settlementDate)
                .orElseGet(() -> dailySettlementRepository.save(
                        DailySettlement.create(creator, settlementDate)
                ));
    }

    private LocalDate toKstDate(OffsetDateTime occurredAt) {
        return occurredAt.atZoneSameInstant(kstClock.zoneId()).toLocalDate();
    }
}
