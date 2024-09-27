package uk.gov.companieshouse.insolvency.delta.processor;

import static uk.gov.companieshouse.insolvency.delta.InsolvencyDeltaConsumerApplication.NAMESPACE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDeleteDelta;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;
import uk.gov.companieshouse.insolvency.delta.service.api.ApiClientService;
import uk.gov.companieshouse.insolvency.delta.transformer.InsolvencyApiTransformer;
import uk.gov.companieshouse.insolvency.delta.validation.InsolvencyDeltaValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class InsolvencyDeltaProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final InsolvencyApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final InsolvencyDeltaValidator validator;

    /**
     * The constructor.
     */
    @Autowired
    public InsolvencyDeltaProcessor(ApiClientService apiClientService,
            InsolvencyApiTransformer transformer,
            InsolvencyDeltaValidator validator) {
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.validator = validator;
    }

    /**
     * Process CHS Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta,
            String topic,
            String partition,
            String offset) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();
        final String companyNumber;

        InsolvencyDelta insolvencyDelta = mapToInsolvencyDelta(payload, InsolvencyDelta.class);

        LOGGER.info("Insolvency delta extracted from payload", DataMapHolder.getLogMap());

        /** We always receive only one insolvency/charge per delta in a list,
         * so we only take the first element
         * CHIPS is not able to send more than one insolvency per delta.
         **/

        Insolvency insolvency = insolvencyDelta.getInsolvency().get(0);

        try {
            validator.validateCaseDates(insolvency);
        } catch (Exception ex) {
            final String msg = "Error when validating case dates";
            LOGGER.error(msg, ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(msg, ex);
        }

        InternalCompanyInsolvency internalCompanyInsolvency = transformer.transform(insolvency);

        LOGGER.info("Message successfully transformed", DataMapHolder.getLogMap());

        final String updatedBy = String.format("%s-%s-%s", topic, partition, offset);

        internalCompanyInsolvency.getInternalData().setUpdatedBy(updatedBy);

        companyNumber = insolvency.getCompanyNumber();

        DataMapHolder.get().companyNumber(companyNumber);
        LOGGER.info("Sending PUT request to Insolvency Data API", DataMapHolder.getLogMap());
        final ApiResponse<Void> response =
                apiClientService.putInsolvency(contextId,
                        companyNumber,
                        internalCompanyInsolvency);

        handleResponse(HttpStatus.valueOf(response.getStatusCode()));
    }

    /**
     * Process Insolvency Delete messages.
     */
    public void processDelete(Message<ChsDelta> chsDelta) {
        final var payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();
        final String companyNumber;

        var insolvencyDeleteDelta = mapToInsolvencyDelta(payload, InsolvencyDeleteDelta.class);

        LOGGER.info("Insolvency delta extracted from payload", DataMapHolder.getLogMap());

        companyNumber = insolvencyDeleteDelta.getCompanyNumber();

        DataMapHolder.get().companyNumber(companyNumber);
        LOGGER.info("Sending DELETE request to Insolvency Data API", DataMapHolder.getLogMap());

        final ApiResponse<Void> response =
                apiClientService.deleteInsolvency(contextId, companyNumber);

        handleDeleteResponse(HttpStatus.valueOf(response.getStatusCode()));
    }

    private <T> T mapToInsolvencyDelta(ChsDelta payload, Class<T> deltaClass)
            throws NonRetryableErrorException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(payload.getData(), deltaClass);
        } catch (Exception ex) {
            LOGGER.error("Failed to map to insolvency delta", ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("Failed to map to insolvency delta", ex);
        }
    }

    private void handleResponse(final HttpStatus httpStatus)
            throws NonRetryableErrorException, RetryableErrorException {
        if (HttpStatus.BAD_REQUEST == httpStatus || HttpStatus.CONFLICT == httpStatus) {
            LOGGER.error(String.format("PUT request to API returned non-retryable: %d", httpStatus.value()),
                    DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(
                    String.format("PUT request to API returned non-retryable: %d", httpStatus.value()));
        } else if (!httpStatus.is2xxSuccessful()) {
            // any other client or server status is retryable
            LOGGER.info(String.format("PUT request to API returned retryable: %d", httpStatus.value()),
                    DataMapHolder.getLogMap());
            throw new RetryableErrorException(
                    String.format("PUT request to API returned retryable: %d", httpStatus.value()));
        } else {
            LOGGER.info("Successful PUT request to insolvency-data-api", DataMapHolder.getLogMap());
        }
    }

    private void handleDeleteResponse(final HttpStatus httpStatus)
            throws NonRetryableErrorException, RetryableErrorException {
        Set<HttpStatus> nonRetryableStatuses =
                Collections.unmodifiableSet(EnumSet.of(
                        HttpStatus.BAD_REQUEST, HttpStatus.CONFLICT));

        if (nonRetryableStatuses.contains(httpStatus)) {
            LOGGER.error(String.format("DELETE request to API returned non-retryable: %d", httpStatus.value()),
                    DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(
                    String.format("DELETE request to API returned non-retryable: %d", httpStatus.value()));
        } else if (!httpStatus.is2xxSuccessful()) {
            // any other client or server status is retryable
            LOGGER.info(String.format("DELETE request to API returned retryable: %d", httpStatus.value()),
                    DataMapHolder.getLogMap());
            throw new RetryableErrorException(
                    String.format("DELETE request to API returned retryable: %d", httpStatus.value()));
        } else {
            LOGGER.info("Successful DELETE request to Insolvency Data API", DataMapHolder.getLogMap());
        }
    }

}
