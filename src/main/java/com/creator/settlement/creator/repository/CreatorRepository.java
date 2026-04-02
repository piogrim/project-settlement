package com.creator.settlement.creator.repository;

import com.creator.settlement.creator.domain.Creator;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorRepository extends JpaRepository<Creator, String> {

    List<Creator> findAllByOrderByIdAsc();
}
