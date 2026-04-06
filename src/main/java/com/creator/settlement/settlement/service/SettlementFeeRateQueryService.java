package com.creator.settlement.settlement.service;

import com.creator.settlement.settlement.dto.response.SettlementFeeRateResult;
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

    public List<SettlementFeeRateResult> getSettlementFeeRates() {
        return settlementFeeRateRepository.findAllByOrderByEffectiveFromDesc().stream()
                .map(SettlementFeeRateResult::from)
                .toList();
    }
}
