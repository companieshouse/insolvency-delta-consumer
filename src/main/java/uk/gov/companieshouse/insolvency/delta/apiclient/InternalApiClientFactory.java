package uk.gov.companieshouse.insolvency.delta.apiclient;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;

@Component
public class InternalApiClientFactory implements Supplier<InternalApiClient> {

    private final String apiKey;
    private final String apiUrl;

    public InternalApiClientFactory(@Value("${api.insolvency-data-api-key}") String apiKey,
            @Value("${api.api-url}") String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public InternalApiClient get() {
        InternalApiClient internalApiClient = new InternalApiClient(new ApiKeyHttpClient(apiKey));
        internalApiClient.setBasePath(apiUrl);
        return internalApiClient;
    }
}
