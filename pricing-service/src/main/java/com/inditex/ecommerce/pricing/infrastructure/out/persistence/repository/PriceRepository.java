package com.inditex.ecommerce.pricing.infrastructure.out.persistence.repository;

import com.inditex.ecommerce.pricing.infrastructure.out.persistence.entity.PriceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceRepository extends JpaRepository<PriceEntity, Long> {

    @Query("""
        SELECT p FROM PriceEntity p
        WHERE p.brandId = :brandId
          AND p.productId = :productId
          AND :applicationDate BETWEEN p.startDate AND p.endDate
        ORDER BY p.priority DESC
        """)
    List<PriceEntity> findTopApplicablePrice(
            @Param("brandId") Long brandId,
            @Param("productId") Long productId,
            @Param("applicationDate") LocalDateTime applicationDate,
            Pageable pageable
    );

    boolean existsByBrandId(Long brandId);

    boolean existsByBrandIdAndProductId(Long brandId, Long productId);
}