package uk.gov.companieshouse.insolvency.delta.service;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;

@Service
public class InsolvencyApiService {

    public ApiResponse<?> invokeInsolvencyApi() {
        //TODO: Change to Provider autowire to inject prototype bean?
        InternalApiClient internalApiClient = getInternalApiClient();
        return null;
    }

    @Lookup
    public InternalApiClient getInternalApiClient() {
        return null;
    }
}
