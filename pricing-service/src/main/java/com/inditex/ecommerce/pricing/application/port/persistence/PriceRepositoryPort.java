package com.inditex.ecommerce.pricing.application.port.persistence;

import com.inditex.ecommerce.pricing.domain.model.Price;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PriceRepositoryPort {
    Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate);

    boolean existsByBrandId(Long brandId);

    boolean existsByBrandIdAndProductId(Long brandId, Long productId);
}