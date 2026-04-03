package com.creator.settlement.settlement.support;

import java.math.BigDecimal;

public final class SettlementPolicy {

    public static final String MIN_FEE_RATE_TEXT = "0.0001";
    public static final String MAX_FEE_RATE_TEXT = "50.0000";
    public static final String FEE_RATE_RANGE_MESSAGE =
            "수수료율은 " + MIN_FEE_RATE_TEXT + " 이상 " + MAX_FEE_RATE_TEXT + " 이하여야 합니다.";
    public static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.20");
    public static final BigDecimal MIN_FEE_RATE = new BigDecimal(MIN_FEE_RATE_TEXT);
    public static final BigDecimal MAX_FEE_RATE = new BigDecimal(MAX_FEE_RATE_TEXT);

    private SettlementPolicy() {
    }
}
