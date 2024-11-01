package uk.gov.companieshouse.insolvency.delta.apiclient;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException.Builder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;

@ExtendWith(MockitoExtension.class)
class ResponseHandlerTest {

    private final ResponseHandler responseHandler = new ResponseHandler();

    @ParameterizedTest
    @CsvSource({
            "401",
            "403",
            "404",
            "405",
            "500",
            "501",
            "502",
            "503",
            "504"
    })
    void shouldHandleApiErrorResponseByThrowingRetryableExceptionForSpecificStatusCodes(final int httpStatusCode) {
        // given
        ApiErrorResponseException exception = new ApiErrorResponseException(
                new Builder(httpStatusCode, "", new HttpHeaders()));

        // when
        Executable executable = () -> responseHandler.handle(exception);

        // then
        assertThrows(RetryableErrorException.class, executable);
    }

    @ParameterizedTest
    @CsvSource({
            "400",
            "409"
    })
    void shouldHandleApiErrorResponseByThrowingNonRetryableExceptionFor400And409(final int httpStatusCode) {
        // given
        ApiErrorResponseException exception = new ApiErrorResponseException(
                new Builder(httpStatusCode, "", new HttpHeaders()));

        // when
        Executable executable = () -> responseHandler.handle(exception);

        // then
        assertThrows(NonRetryableErrorException.class, executable);
    }

    @Test
    void shouldHandleUriValidationExceptionByThrowingNonRetryableException() {
        // given
        URIValidationException exception = new URIValidationException("");

        // when
        Executable executable = () -> responseHandler.handle(exception);

        // then
        assertThrows(NonRetryableErrorException.class, executable);
    }
}
