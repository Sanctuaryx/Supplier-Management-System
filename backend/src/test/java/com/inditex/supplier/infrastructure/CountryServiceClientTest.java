package com.inditex.supplier.infrastructure;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.inditex.supplier.domain.exception.CountryNotFoundException;
import com.inditex.supplier.domain.model.Country;
import com.inditex.supplier.infrastructure.rest.client.CountryServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("null")
class CountryServiceClientTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    CountryServiceClient client() {
        RestClient restClient = RestClient.builder()
                .baseUrl(wm.baseUrl())
                .build();
        return new CountryServiceClient(restClient);
    }

    @Test
    void returnsFalse_whenCountryNotBanned() {
        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/countries/ES"))
                .willReturn(WireMock.okJson("{\"name\":\"ES\",\"isBanned\":false}")));

        assertThat(client().isBanned(new Country("ES"))).isFalse();
    }

    @Test
    void returnsTrue_whenCountryBanned() {
        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/countries/ZZ"))
                .willReturn(WireMock.okJson("{\"name\":\"ZZ\",\"isBanned\":true}")));

        assertThat(client().isBanned(new Country("ZZ"))).isTrue();
    }

    @Test
    void throwsCountryNotFoundException_on404() {
        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/countries/XX"))
                .willReturn(WireMock.notFound()));

        assertThatThrownBy(() -> client().isBanned(new Country("XX")))
                .isInstanceOf(CountryNotFoundException.class);
    }
}
