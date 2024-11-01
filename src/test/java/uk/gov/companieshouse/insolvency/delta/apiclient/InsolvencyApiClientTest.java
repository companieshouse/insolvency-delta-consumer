package uk.gov.companieshouse.insolvency.delta.apiclient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.insolvency.request.PrivateInsolvencyDelete;
import uk.gov.companieshouse.api.handler.delta.insolvency.request.PrivateInsolvencyUpsert;
import uk.gov.companieshouse.api.handler.delta.insolvency.request.PrivateInsolvencyUpsertResourceHandler;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;

@ExtendWith(MockitoExtension.class)
class InsolvencyApiClientTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_URI = String.format("/company/%s/insolvency", COMPANY_NUMBER);
    private static final String CONTEXT_ID = "context_id";

    @InjectMocks
    private InsolvencyApiClient insolvencyApiClient;

    @Mock
    private InternalApiClientFactory internalApiClientFactory;
    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private InternalCompanyInsolvency requestBody;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private HttpClient apiClient;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PrivateInsolvencyUpsertResourceHandler privateInsolvencyUpsertResourceHandler;
    @Mock
    private PrivateInsolvencyUpsert privateInsolvencyUpsert;
    @Mock
    private PrivateInsolvencyDelete privateInsolvencyDelete;

    @BeforeEach
    void whenApiClientStubbing() {
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
    }

    @AfterEach
    void verifyApiClientCalls() {
        verify(apiClient).setRequestId(CONTEXT_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
    }

    @Test
    void shouldSendSuccessfulPutRequest() throws ApiErrorResponseException, URIValidationException {
        // given
        DataMapHolder.get().requestId(CONTEXT_ID);

        when(privateDeltaResourceHandler.putInsolvency()).thenReturn(privateInsolvencyUpsertResourceHandler);
        when(privateInsolvencyUpsertResourceHandler.upsert(any(), any())).thenReturn(privateInsolvencyUpsert);

        // when
        insolvencyApiClient.putInsolvency(COMPANY_NUMBER, requestBody);

        // then
        verifyNoInteractions(responseHandler);
        verify(privateDeltaResourceHandler).putInsolvency();
        verify(privateInsolvencyUpsertResourceHandler).upsert(REQUEST_URI, requestBody);
        verify(privateInsolvencyUpsert).execute();
    }

    @Test
    void shouldHandleApiErrorExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<ApiErrorResponseException> exceptionClass = ApiErrorResponseException.class;

        when(privateDeltaResourceHandler.putInsolvency()).thenReturn(privateInsolvencyUpsertResourceHandler);
        when(privateInsolvencyUpsertResourceHandler.upsert(any(), any())).thenReturn(privateInsolvencyUpsert);
        when(privateInsolvencyUpsert.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(CONTEXT_ID);

        // when
        insolvencyApiClient.putInsolvency(COMPANY_NUMBER, requestBody);

        // then
        verify(responseHandler).handle(any(exceptionClass));
        verify(privateDeltaResourceHandler).putInsolvency();
        verify(privateInsolvencyUpsertResourceHandler).upsert(REQUEST_URI, requestBody);
        verify(privateInsolvencyUpsert).execute();
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        when(privateDeltaResourceHandler.putInsolvency()).thenReturn(privateInsolvencyUpsertResourceHandler);
        when(privateInsolvencyUpsertResourceHandler.upsert(any(), any())).thenReturn(privateInsolvencyUpsert);
        when(privateInsolvencyUpsert.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(CONTEXT_ID);

        // when
        insolvencyApiClient.putInsolvency(COMPANY_NUMBER, requestBody);

        // then
        verify(responseHandler).handle(any(exceptionClass));
        verify(privateDeltaResourceHandler).putInsolvency();
        verify(privateInsolvencyUpsertResourceHandler).upsert(REQUEST_URI, requestBody);
        verify(privateInsolvencyUpsert).execute();
    }

    @Test
    void shouldSendSuccessfulDeleteRequest() {
        // given
        DataMapHolder.get().requestId(CONTEXT_ID);

        when(privateDeltaResourceHandler.deleteInsolvency(any())).thenReturn(privateInsolvencyDelete);

        // when
        insolvencyApiClient.deleteInsolvency(COMPANY_NUMBER);

        // then
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldHandleApiErrorExceptionWhenSendingDeleteRequest() throws Exception {
        // given
        Class<ApiErrorResponseException> exceptionClass = ApiErrorResponseException.class;

        when(privateDeltaResourceHandler.deleteInsolvency(any())).thenReturn(privateInsolvencyDelete);
        when(privateInsolvencyDelete.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(CONTEXT_ID);

        // when
        insolvencyApiClient.deleteInsolvency(COMPANY_NUMBER);

        // then
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingDeleteRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        when(privateDeltaResourceHandler.deleteInsolvency(any())).thenReturn(privateInsolvencyDelete);
        when(privateInsolvencyDelete.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(CONTEXT_ID);

        // when
        insolvencyApiClient.deleteInsolvency(COMPANY_NUMBER);

        // then
        verify(responseHandler).handle(any(exceptionClass));
    }
}