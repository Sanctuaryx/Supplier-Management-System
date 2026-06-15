package com.inditex.supplier.infrastructure;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@SuppressWarnings("null")
class SupplierIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("country-service.base-url", wm::baseUrl);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void fullLifecycle_createAcceptGetSupplier() throws Exception {
        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/countries/ES"))
                .willReturn(WireMock.okJson("{\"name\":\"ES\",\"isBanned\":false}")));

        mockMvc.perform(post("/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"duns":111222333,"name":"Acme Corp","country":"ES","annualTurnover":2000000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duns").value(111222333));

        mockMvc.perform(get("/candidates/111222333"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Corp"));

        mockMvc.perform(post("/candidates/111222333/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sustainabilityRating":"A"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/suppliers/111222333"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.sustainabilityRating").value("A"));

        mockMvc.perform(post("/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"duns":111222333,"name":"Acme Corp","country":"ES","annualTurnover":2000000}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void acceptBannedCountry_returns409() throws Exception {
        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/countries/ZZ"))
                .willReturn(WireMock.okJson("{\"name\":\"ZZ\",\"isBanned\":true}")));

        mockMvc.perform(post("/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"duns":222333444,"name":"Banned Corp","country":"ZZ","annualTurnover":2000000}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/candidates/222333444/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sustainabilityRating":"A"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void potentialSuppliers_scoreAndBonusCalculation() throws Exception {
        wm.stubFor(WireMock.get(WireMock.urlPathMatching("/countries/.*"))
                .willReturn(WireMock.okJson("{\"name\":\"ES\",\"isBanned\":false}")));

        createAndAccept(333444555, "S1", "ES", 1_000_000L, "A");
        createAndAccept(444555666, "S2", "ES", 1_000_000L, "B");
        createAndAccept(555666777, "S3", "ES", 2_000_000L, "A");

        mockMvc.perform(get("/suppliers/potential?rate=500000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data[0].score").exists());
    }

    @Test
    void banSupplier_onProbation_then_cannotReapply() throws Exception {
        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/countries/ES"))
                .willReturn(WireMock.okJson("{\"name\":\"ES\",\"isBanned\":false}")));

        createAndAccept(666777888, "Probation Corp", "ES", 1_500_000L, "C");

        mockMvc.perform(post("/suppliers/666777888/ban"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/suppliers/666777888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Disqualified"));

        mockMvc.perform(post("/suppliers/666777888/ban"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"duns":666777888,"name":"Probation Corp","country":"ES","annualTurnover":1500000}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.info").value("Supplier banned"));
    }

    @Test
    void createCandidate_unknownCountry_returns422() throws Exception {
        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/countries/XX"))
                .willReturn(WireMock.notFound()));

        mockMvc.perform(post("/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"duns":777888999,"name":"Unknown Country Corp","country":"XX","annualTurnover":2000000}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    private void createAndAccept(int duns, String name, String country, long turnover, String rating) throws Exception {
        mockMvc.perform(post("/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"duns\":%d,\"name\":\"%s\",\"country\":\"%s\",\"annualTurnover\":%d}",
                                duns, name, country, turnover)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/candidates/" + duns + "/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"sustainabilityRating\":\"%s\"}", rating)))
                .andExpect(status().isNoContent());
    }
}
