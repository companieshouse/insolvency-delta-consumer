package uk.gov.companieshouse.insolvency.delta.service.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.model.ApiResponse;

/**
 * Service that sends REST requests via private SDK.
 */
@Primary
@Service
public class ApiClientServiceImpl extends BaseApiClientServiceImpl implements ApiClientService {

    private final String chsApiKey;
    private final String apiUrl;
    private final String internalApiUrl;

    public ApiClientServiceImpl(
            @Value("${api.insolvency-data-api-key}") String chsApiKey,
            @Value("${api.api-url}") String apiUrl,
            @Value("${api.internal-api-url}") String internalApiUrl) {
        this.chsApiKey = chsApiKey;
        this.apiUrl = apiUrl;
        this.internalApiUrl = internalApiUrl;
    }

    @Override
    public InternalApiClient getApiClient(final String contextId) {
        InternalApiClient internalApiClient = new InternalApiClient(getHttpClient(contextId));
        internalApiClient.setBasePath(apiUrl);
        internalApiClient.setInternalBasePath(internalApiUrl);

        return internalApiClient;
    }

    private HttpClient getHttpClient(String contextId) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(chsApiKey);
        httpClient.setRequestId(contextId);
        return httpClient;
    }

    @Override
    public ApiResponse<Void> putInsolvency(
            final String contextId,
            String companyNumber,
            InternalCompanyInsolvency insolvency) {
        final String uri = String.format("/company/%s/insolvency", companyNumber);

        return executeOp(getApiClient(contextId).privateDeltaInsolvencyResourceHandler()
                .putInsolvency()
                .upsert(uri, insolvency));
    }

    @Override
    public ApiResponse<Void> deleteInsolvency(
            final String contextId,
            final String companyNumber) {
        final String uri = String.format("/company/%s/insolvency", companyNumber);

        return executeOp(getApiClient(contextId).privateDeltaInsolvencyResourceHandler().deleteInsolvency(uri));
    }
}
