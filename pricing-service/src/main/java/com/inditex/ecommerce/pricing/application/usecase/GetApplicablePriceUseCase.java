package com.inditex.ecommerce.pricing.application.usecase;

import com.inditex.ecommerce.pricing.application.exception.PriceNotFoundException;
import com.inditex.ecommerce.pricing.application.port.persistence.PriceRepositoryPort;
import com.inditex.ecommerce.pricing.domain.model.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepositoryPort;

    /**
     * Busca el precio aplicable para una cadena, producto y fecha de aplicación.
     * Si no existe ningún precio vigente en ese momento, lanza {@link PriceNotFoundException}.
     *
     * @param brandId         identificador de la cadena/marca
     * @param productId       identificador del producto
     * @param applicationDate fecha y hora en la que se quiere consultar el precio
     * @return el precio aplicable con mayor prioridad
     * @throws PriceNotFoundException si no hay precio aplicable para los parámetros dados
     */
    public Price execute(Long brandId, Long productId, LocalDateTime applicationDate) {
        log.debug("Buscando precio aplicable para brandId={}, productId={}, applicationDate={}", brandId, productId, applicationDate);
        return priceRepositoryPort.findApplicablePrice(brandId, productId, applicationDate)
                .orElseThrow(() -> new PriceNotFoundException(brandId, productId, applicationDate));
    }
}
