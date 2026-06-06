package com.inditex.ecommerce.pricing.application.mapper;

import com.inditex.ecommerce.pricing.domain.model.Price;
import com.inditex.ecommerce.pricing.infrastructure.in.rest.dto.PriceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PriceMapper {
    PriceResponse toResponse(Price price);
}