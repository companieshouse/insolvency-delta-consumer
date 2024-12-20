package uk.gov.companieshouse.insolvency.delta.apiclient;


import static uk.gov.companieshouse.insolvency.delta.InsolvencyDeltaConsumerApplication.APPLICATION_NAMESPACE;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private static final String API_INFO_RESPONSE_MESSAGE = "Call to insolvency-data-api failed, status code: %d. %s";
    private static final String API_ERROR_RESPONSE_MESSAGE = "Call to insolvency-data-api failed, status code: %d";
    private static final String URI_VALIDATION_EXCEPTION_MESSAGE = "Invalid URI";

    public void handle(ApiErrorResponseException ex) {
        final int statusCode = ex.getStatusCode();
        HttpStatus httpStatus = HttpStatus.valueOf(ex.getStatusCode());

        if (HttpStatus.CONFLICT.equals(httpStatus) || HttpStatus.BAD_REQUEST.equals(httpStatus)) {
            String errorMsg = String.format(API_ERROR_RESPONSE_MESSAGE, statusCode);
            LOGGER.error(errorMsg, ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(errorMsg, ex);
        } else {
            LOGGER.info(String.format(API_INFO_RESPONSE_MESSAGE, statusCode, Arrays.toString(ex.getStackTrace())),
                    DataMapHolder.getLogMap());
            throw new RetryableErrorException(String.format(API_ERROR_RESPONSE_MESSAGE, statusCode), ex);
        }
    }

    public void handle(URIValidationException ex) {
        LOGGER.error(URI_VALIDATION_EXCEPTION_MESSAGE, DataMapHolder.getLogMap());
        throw new NonRetryableErrorException(URI_VALIDATION_EXCEPTION_MESSAGE, ex);
    }
}
