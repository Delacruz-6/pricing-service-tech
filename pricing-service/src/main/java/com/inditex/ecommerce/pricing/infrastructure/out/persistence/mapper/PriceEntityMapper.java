package com.inditex.ecommerce.pricing.infrastructure.out.persistence.mapper;

import com.inditex.ecommerce.pricing.domain.model.Price;
import com.inditex.ecommerce.pricing.infrastructure.out.persistence.entity.PriceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PriceEntityMapper {

    @Mapping(target = "currency", expression = "java(entity.getCurr() != null ? Currency.getInstance(entity.getCurr()) : null)")
    Price toDomain(PriceEntity entity);

    @Mapping(target = "curr", expression = "java(price.currency() != null ? price.currency().getCurrencyCode() : null)")
    @Mapping(target = "id", ignore = true)
    PriceEntity toEntity(Price price);
}