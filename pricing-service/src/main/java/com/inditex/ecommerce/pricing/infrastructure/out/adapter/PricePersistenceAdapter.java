package com.inditex.ecommerce.pricing.infrastructure.out.adapter;

import com.inditex.ecommerce.pricing.application.port.persistence.PriceRepositoryPort;
import com.inditex.ecommerce.pricing.domain.model.Price;
import com.inditex.ecommerce.pricing.infrastructure.out.persistence.mapper.PriceEntityMapper;
import com.inditex.ecommerce.pricing.infrastructure.out.persistence.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PricePersistenceAdapter implements PriceRepositoryPort {

    private final PriceRepository repository;
    private final PriceEntityMapper mapper;

    @Override
    public Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
        // PageRequest en lugar de LIMIT: JPQL estandar no lo soporta y ya ordenamos por prioridad DESC
        return repository.findTopApplicablePrice(brandId, productId, applicationDate, PageRequest.of(0, 1))
                .stream().findFirst().map(mapper::toDomain);
    }
}