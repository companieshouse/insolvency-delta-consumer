package uk.gov.companieshouse.insolvency.delta.service.api;

import static uk.gov.companieshouse.insolvency.delta.InsolvencyDeltaConsumerApplication.NAMESPACE;

import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.Executor;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public abstract class BaseApiClientServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public <T> ApiResponse<T> executeOp(final Executor<ApiResponse<T>> executor) {
        try {
            return executor.execute();
        } catch (URIValidationException ex) {
            LOGGER.error("URI Validation Exception", ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("URI Validation Exception", ex);
        } catch (ApiErrorResponseException ex) {
            if (ex.getStatusCode() != 0) {
                return new ApiResponse<>(ex.getStatusCode(), null);
            }
            LOGGER.info("SDK exception", DataMapHolder.getLogMap());
            throw new RetryableErrorException("SDK Exception", ex);
        }
    }
}
