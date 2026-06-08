package com.inditex.ecommerce.pricing.application.usecase;

import com.inditex.ecommerce.pricing.application.exception.PriceNotFoundException;
import com.inditex.ecommerce.pricing.application.port.persistence.PriceRepositoryPort;
import com.inditex.ecommerce.pricing.domain.model.Price;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepositoryPort;

    /**
     * Busca el precio aplicable para una cadena, producto y fecha de aplicación.
     * Si no existe ningún precio vigente en ese momento, lanza {@link PriceNotFoundException}
     * con un mensaje acorde al motivo (cadena, producto o fecha sin tarifa).
     *
     * @param brandId         identificador de la cadena/marca
     * @param productId       identificador del producto
     * @param applicationDate fecha y hora en la que se quiere consultar el precio
     * @return El precio aplicable con mayor prioridad
     * @throws PriceNotFoundException si no hay precio aplicable para los parámetros dados
     */
    public Price execute(Long brandId, Long productId, LocalDateTime applicationDate) {
        return priceRepositoryPort.findApplicablePrice(brandId, productId, applicationDate)
                .orElseThrow(() -> resolveNotFound(brandId, productId, applicationDate));
    }

    private PriceNotFoundException resolveNotFound(Long brandId, Long productId, LocalDateTime applicationDate) {

        if (!priceRepositoryPort.existsByBrandId(brandId)) {
            throw PriceNotFoundException.brandNotFound(brandId);
        }
        if (!priceRepositoryPort.existsByBrandIdAndProductId(brandId, productId)) {
            return PriceNotFoundException.productNotFound(brandId, productId);
        }

        return PriceNotFoundException.noPriceForDate(brandId, productId, applicationDate);
    }

}

