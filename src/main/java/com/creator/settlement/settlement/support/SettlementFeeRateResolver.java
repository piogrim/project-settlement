package com.creator.settlement.settlement.support;

import com.creator.settlement.settlement.repository.SettlementFeeRateRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementFeeRateResolver {

    private final SettlementFeeRateRepository settlementFeeRateRepository;

    public BigDecimal resolve(YearMonth settlementMonth) {
        return settlementFeeRateRepository.findFirstByEffectiveFromLessThanEqualOrderByEffectiveFromDesc(settlementMonth)
                .map(settlementFeeRate -> settlementFeeRate.getFeeRate())
                .orElse(SettlementPolicy.DEFAULT_FEE_RATE);
    }
}
