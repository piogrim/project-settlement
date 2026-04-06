package com.creator.settlement.creator.repository;

import com.creator.settlement.creator.domain.Creator;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreatorRepository extends JpaRepository<Creator, String> {

    List<Creator> findAllByOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select creator
            from Creator creator
            where creator.id = :creatorId
            """)
    Optional<Creator> findByIdForUpdate(@Param("creatorId") String creatorId);
}
