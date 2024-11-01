package uk.gov.companieshouse.insolvency.delta.apiclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.InternalApiClient;

class InternalApiClientFactoryTest {

    private static final String API_KEY = "api key";
    private static final String API_URL = "url";

    private final InternalApiClientFactory factory = new InternalApiClientFactory(API_KEY, API_URL);

    @Test
    void shouldReturnNewInternalApiClient() {
        // given

        // when
        InternalApiClient actual = factory.get();

        // then
        assertEquals(API_URL, actual.getBasePath());
        assertNotNull(actual.getHttpClient());
    }
}