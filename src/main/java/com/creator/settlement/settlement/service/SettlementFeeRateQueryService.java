package com.creator.settlement.settlement.service;

import com.creator.settlement.settlement.dto.response.SettlementFeeRateHistoryResult;
import com.creator.settlement.settlement.repository.SettlementFeeRateRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementFeeRateQueryService {

    private final SettlementFeeRateRepository settlementFeeRateRepository;

    public List<SettlementFeeRateHistoryResult> getSettlementFeeRates() {
        return settlementFeeRateRepository.findAllByOrderByEffectiveFromDesc().stream()
                .map(SettlementFeeRateHistoryResult::from)
                .toList();
    }
}
