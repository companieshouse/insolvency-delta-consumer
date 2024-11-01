package uk.gov.companieshouse.insolvency.delta.apiclient;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;

@Component
public class InsolvencyApiClient implements ApiClient {

    private static final String REQUEST_URI = "/company/%s/insolvency";

    private final InternalApiClientFactory internalApiClientFactory;
    private final ResponseHandler responseHandler;

    public InsolvencyApiClient(InternalApiClientFactory internalApiClientFactory, ResponseHandler responseHandler) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.responseHandler = responseHandler;
    }

    @Override
    public void putInsolvency(String companyNumber, InternalCompanyInsolvency requestBody) {
        InternalApiClient client = internalApiClientFactory.get();
        client.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        final String formattedUri = String.format(REQUEST_URI, companyNumber);
        try {
            client.privateDeltaResourceHandler()
                    .putInsolvency()
                    .upsert(formattedUri, requestBody)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
    }

    @Override
    public void deleteInsolvency(String companyNumber, String deltaAt) {
        InternalApiClient client = internalApiClientFactory.get();
        client.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        final String formattedUri = String.format(REQUEST_URI, companyNumber);
        try {
            client.privateDeltaResourceHandler()
                    .deleteInsolvency(formattedUri, deltaAt)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
    }
}
