package uk.gov.companieshouse.insolvency.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDeleteDelta;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.service.api.ApiClientService;
import uk.gov.companieshouse.insolvency.delta.transformer.InsolvencyApiTransformer;
import uk.gov.companieshouse.insolvency.delta.validation.InsolvencyDeltaValidator;
import uk.gov.companieshouse.logging.Logger;

@Component
public class InsolvencyDeltaProcessor {

    private final InsolvencyApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final Logger logger;
    private final InsolvencyDeltaValidator validator;

    /**
     * The constructor.
     */
    @Autowired
    public InsolvencyDeltaProcessor(ApiClientService apiClientService,
                                    InsolvencyApiTransformer transformer,
                                    Logger logger,
                                    InsolvencyDeltaValidator validator) {
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.logger = logger;
        this.validator = validator;
    }

    /**
     * Process CHS Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta,
                             String topic,
                             String partition,
                             String offset) {
        MessageHeaders headers = chsDelta.getHeaders();
        final ChsDelta payload = chsDelta.getPayload();
        final String logContext = payload.getContextId();
        final Map<String, Object> logMap = new HashMap<>();
        final String companyNumber;

        InsolvencyDelta insolvencyDelta =
                mapToInsolvencyDelta(payload, logContext, InsolvencyDelta.class);

        logger.trace(String.format("InsolvencyDelta extracted for context ID %s "
                + "a Kafka message: %s",
                logContext,
                insolvencyDelta));


        /** We always receive only one insolvency/charge per delta in a list,
         * so we only take the first element
         * CHIPS is not able to send more than one insolvency per delta.
         **/

        Insolvency insolvency = insolvencyDelta.getInsolvency().get(0);

        try {
            validator.validateCaseDates(insolvency);
        } catch (Exception error) {
            throw new NonRetryableErrorException("Error when validating case dates: "
                    + error.getMessage());
        }

        InternalCompanyInsolvency internalCompanyInsolvency = transformer.transform(insolvency);

        logger.trace(String.format("Message with contextId: %s successfully "
                        + "transformed into InsolvencyAPI object",
                logContext));

        final String updatedBy = String.format("%s-%s-%s", topic, partition, offset);

        internalCompanyInsolvency.getInternalData().setUpdatedBy(updatedBy);

        companyNumber = insolvency.getCompanyNumber();

        logger.trace(String.format("Performing a PUT with "
                + "company number %s for contextId %s", companyNumber, logContext));
        final ApiResponse<Void> response =
                apiClientService.putInsolvency(logContext,
                        companyNumber,
                        internalCompanyInsolvency);

        handleResponse(HttpStatus.valueOf(response.getStatusCode()), logContext,
                "Response from sending insolvency data", logMap);

    }

    /**
     * Process Insolvency Delete messages.
     */
    public void processDelete(Message<ChsDelta> chsDelta) {
        final var payload = chsDelta.getPayload();
        final String logContext = payload.getContextId();
        final Map<String, Object> logMap = new HashMap<>();
        final String companyNumber;

        var insolvencyDeleteDelta =
                mapToInsolvencyDelta(payload, logContext, InsolvencyDeleteDelta.class);

        logger.trace(String.format("InsolvencyDeleteDelta extracted for context ID %s "
                        + "a Kafka message: %s",
                logContext,
                insolvencyDeleteDelta));

        companyNumber = insolvencyDeleteDelta.getCompanyNumber();
        logMap.put("company_number", companyNumber);

        logger.trace(String.format("Performing a DELETE with "
                + "company number %s for contextId %s", companyNumber, logContext));
        final ApiResponse<Void> response =
                apiClientService.deleteInsolvency(logContext, companyNumber);

        handleDeleteResponse(HttpStatus.valueOf(response.getStatusCode()), logContext,
                logMap);
    }

    private <T> T mapToInsolvencyDelta(ChsDelta payload, String contextId, Class<T> deltaClass)
            throws NonRetryableErrorException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(payload.getData(), deltaClass);
        } catch (Exception exception) {
            logger.error(String.format("Failed to map to insolvency delta"
                            + " class for context ID %s",
                    contextId));
            throw new NonRetryableErrorException(
                    "Error when mapping to insolvency delta class" + deltaClass.getName(),
                    exception);
        }
    }

    private void handleResponse(
            final HttpStatus httpStatus,
            final String logContext,
            final String msg,
            final Map<String, Object> logMap)
            throws NonRetryableErrorException, RetryableErrorException {
        logMap.put("status", httpStatus.toString());
        if (HttpStatus.BAD_REQUEST == httpStatus) {
            // 400 BAD REQUEST status is not retryable
            throw new NonRetryableErrorException(String
                    .format("Bad request PUT Api Response %s", msg));
        } else if (!httpStatus.is2xxSuccessful()) {
            // any other client or server status is retryable
            logger.error(String.format("Failed to invoke insolvency-data-api "
                            + "PUT endpoint for message with contextId: %s "
                            + "with error %s",
                    logContext,
                    msg));
            throw new RetryableErrorException(String
                    .format("Unsuccessful PUT API response, %s", msg));
        } else {
            logger.info(String.format("Successfully invoked insolvency-data-api "
                    + "PUT endpoint for message with contextId: %s",
                    logContext));
        }
    }

    private void handleDeleteResponse(
            final HttpStatus httpStatus,
            final String logContext,
            final Map<String, Object> logMap)
            throws NonRetryableErrorException, RetryableErrorException {
        logMap.put("status", httpStatus.toString());
        String msg = "Response from DELETE insolvency request";
        Set<HttpStatus> nonRetryableStatuses =
                Collections.unmodifiableSet(EnumSet.of(
                        HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND));

        if (nonRetryableStatuses.contains(httpStatus)) {
            throw new NonRetryableErrorException(
                    String.format("Bad request DELETE Api Response %s", msg));
        } else if (!httpStatus.is2xxSuccessful()) {
            // any other client or server status is retryable
            logger.error(String.format("Failed to invoke insolvency-data-api "
                            + "DELETE endpoint for message with contextId: %s "
                            + "with error %s",
                    logContext,
                    msg));
            throw new RetryableErrorException(
                    String.format("Unsuccessful DELETE API response, %s", msg));
        } else {
            logger.info(String.format("Successfully invoked insolvency-data-api "
                            + "DELETE endpoint for message with contextId: %s",
                    logContext));
        }
    }

}
