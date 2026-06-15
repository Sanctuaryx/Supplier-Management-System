package com.inditex.supplier.domain.port;

import com.inditex.supplier.domain.exception.CountryNotFoundException;
import com.inditex.supplier.domain.model.Country;

/**
 * Outbound port for querying country ban status from an external service.
 * The implementation delegates to the Inditex country lookup HTTP API.
 */
public interface CountryService {

    /**
     * Checks whether the given country is on Inditex's banned-countries list.
     * A banned country disqualifies any candidate from that country from being accepted.
     *
     * @param country the country to check (ISO 3166-1 alpha-2)
     * @return {@code true} if the country is banned, {@code false} otherwise
     * @throws CountryNotFoundException if the external service does not recognise the country code
     */
    boolean isBanned(Country country);
}
