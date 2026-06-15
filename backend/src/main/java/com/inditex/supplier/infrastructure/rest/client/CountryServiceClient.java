package com.inditex.supplier.infrastructure.rest.client;

import com.inditex.supplier.domain.exception.CountryNotFoundException;
import com.inditex.supplier.domain.model.Country;
import com.inditex.supplier.domain.port.CountryService;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP adapter that implements {@link CountryService} by calling the Inditex
 * country lookup REST API (configured via {@code country-service.base-url}).
 *
 * <p>The external API contract is defined in
 * {@code wiki/itx-iop_tech-supplier_flow-country-openapi3_1.yaml}.
 * In local/test environments the service is mocked by WireMock on port 8088.
 */
@Component
public class CountryServiceClient implements CountryService {

    private final RestClient restClient;

    /**
     * @param countryRestClient pre-configured {@link RestClient} bean with the country service base URL
     */
    public CountryServiceClient(RestClient countryRestClient) {
        this.restClient = countryRestClient;
    }

    /**
     * Calls {@code GET /countries/{code}} on the external country service and returns
     * whether the country is banned.
     *
     * @param country the country to check (ISO 3166-1 alpha-2)
     * @return {@code true} if the country is banned, {@code false} if it is allowed
     * @throws CountryNotFoundException if the external service returns 404 (unknown country code)
     */
    @Override
    public boolean isBanned(Country country) {
        CountryResponse response = restClient.get()
                .uri("/countries/{code}", country.code())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new CountryNotFoundException(country.code());
                })
                .body(CountryResponse.class);

        if (response == null) {
            throw new CountryNotFoundException(country.code());
        }
        return response.isBanned();
    }
}
