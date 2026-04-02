package com.creator.settlement.sale.service;

import com.creator.settlement.common.exception.ResourceNotFoundException;
import com.creator.settlement.common.time.KstPeriodResolver;
import com.creator.settlement.creator.repository.CreatorRepository;
import com.creator.settlement.sale.dto.SaleRecordResult;
import com.creator.settlement.sale.dto.SaleRecordSearchCriteria;
import com.creator.settlement.sale.repository.SaleRecordRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleQueryService {

    private final CreatorRepository creatorRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final KstPeriodResolver kstPeriodResolver;

    public List<SaleRecordResult> getSaleRecords(@NotNull @Valid SaleRecordSearchCriteria criteria) {
        requireCreator(criteria.creatorId());

        KstPeriodResolver.KstRange paidAtRange =
                kstPeriodResolver.optionalDateRange(criteria.startDate(), criteria.endDate());

        return saleRecordRepository.findSaleRecordsByCreatorAndPaidAtRange(
                        criteria.creatorId(),
                        paidAtRange.startAt(),
                        paidAtRange.endExclusive()
                ).stream()
                .map(SaleRecordResult::from)
                .toList();
    }

    private void requireCreator(String creatorId) {
        if (!creatorRepository.existsById(creatorId)) {
            throw new ResourceNotFoundException("크리에이터를 찾을 수 없습니다: " + creatorId);
        }
    }
}
