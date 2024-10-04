package uk.gov.companieshouse.insolvency.delta.service;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;

@Service
public class InsolvencyApiService {

    /**
     * Invoke Insolvency API.
     */
    public ApiResponse<?> invokeInsolvencyApi() {
        InternalApiClient internalApiClient = getInternalApiClient();
        internalApiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        internalApiClient.setBasePath("apiUrl");

        return null;
    }

    @Lookup
    public InternalApiClient getInternalApiClient() {
        return null;
    }
}