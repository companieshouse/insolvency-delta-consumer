package uk.gov.companieshouse.insolvency.delta.service.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service that sends REST requests via private SDK.
 */
@Primary
@Service
public class ApiClientServiceImpl extends BaseApiClientServiceImpl implements ApiClientService {

    @Value("${api.insolvency-data-api-key}")
    private String chsApiKey;

    @Value("${api.api-url}")
    private String apiUrl;

    @Value("${api.internal-api-url}")
    private String internalApiUrl;

    /**
     * Construct an {@link ApiClientServiceImpl}.
     *
     * @param logger the CH logger
     */
    @Autowired
    public ApiClientServiceImpl(final Logger logger) {
        super(logger);
    }

    @Override
    public InternalApiClient getApiClient(String contextId) {
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
            final String log,
            String companyNumber,
            InternalCompanyInsolvency insolvency) {
        final String uri =
                String.format("/company/%s/insolvency", companyNumber);

        Map<String,Object> logMap = createLogMap(companyNumber,"PUT", uri);
        logger.infoContext(log, String.format("PUT %s", uri), logMap);

        return executeOp(log, "putInsolvency", uri,
                getApiClient(log).privateDeltaInsolvencyResourceHandler()
                        .putInsolvency()
                        .upsert(uri, insolvency));
    }

    @Override
    public ApiResponse<Void> deleteInsolvency(
            final String log,
            final String companyNumber) {
        final String uri =
                String.format("/company/%s/insolvency", companyNumber);

        Map<String,Object> logMap = createLogMap(companyNumber,"DELETE", uri);
        logger.infoContext(log, String.format("DELETE %s", uri), logMap);

        return executeOp(log, "deleteInsolvency", uri,
                getApiClient(log).privateDeltaInsolvencyResourceHandler()
                        .deleteInsolvency(uri));
    }

    private Map<String,Object> createLogMap(String companyNumber, String method, String path) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("company_number", companyNumber);
        logMap.put("method",method);
        logMap.put("path", path);
        return logMap;
    }
}
