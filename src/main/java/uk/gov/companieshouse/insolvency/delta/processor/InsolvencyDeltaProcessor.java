package uk.gov.companieshouse.insolvency.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
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
import uk.gov.companieshouse.logging.Logger;

@Component
public class InsolvencyDeltaProcessor {

    private final InsolvencyApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final Logger logger;

    /**
     * The constructor.
     */
    @Autowired
    public InsolvencyDeltaProcessor(ApiClientService apiClientService,
                                    InsolvencyApiTransformer transformer,
                                    Logger logger) {
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.logger = logger;
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

        if (Boolean.TRUE.equals(payload.getIsDelete())) {
            var insolvencyDeleteDelta =
                    mapToInsolvencyDelta(payload, InsolvencyDeleteDelta.class);
            logger.trace(String.format("InsolvencyDeleteDelta extracted from Kafka message: %s",
                    insolvencyDeleteDelta));

            companyNumber = insolvencyDeleteDelta.getCompanyNumber();
            logMap.put("company_number", companyNumber);

            logger.infoContext(
                    logContext,
                    String.format(
                            "Process DELETE insolvency for company number %s", companyNumber),
                    logMap);

            final ApiResponse<Void> response =
                    apiClientService.deleteInsolvency(logContext, companyNumber);

            handleResponse(null, HttpStatus.valueOf(response.getStatusCode()), logContext,
                    "Response from DELETE insolvency request", logMap);
            return;
        }

        InsolvencyDelta insolvencyDelta = mapToInsolvencyDelta(payload, InsolvencyDelta.class);

        logger.trace(String.format("DSND-362: InsolvencyDelta extracted "
                + "from a Kafka message: %s", insolvencyDelta));

        /** We always receive only one insolvency/charge per delta in a list,
         * so we only take the first element
         * CHIPS is not able to send more than one insolvency per delta.
         **/

        Insolvency insolvency = insolvencyDelta.getInsolvency().get(0);

        InternalCompanyInsolvency internalCompanyInsolvency = transformer.transform(insolvency);

        final String updatedBy = String.format("%s-%s-%s", topic, partition, offset);

        internalCompanyInsolvency.getInternalData().setUpdatedBy(updatedBy);

        companyNumber = insolvency.getCompanyNumber();
        logger.trace(String.format("DSND-362: InsolvencyDelta transformed into "
                + "InternalCompanyInsolvency: %s", internalCompanyInsolvency));
        logMap.put("company_number", companyNumber);
        logger.infoContext(
                logContext,
                String.format("Process insolvency for company number [%s]", companyNumber),
                logMap);

        final ApiResponse<Void> response =
                apiClientService.putInsolvency(logContext,
                        companyNumber,
                        internalCompanyInsolvency);

        handleResponse(null, HttpStatus.valueOf(response.getStatusCode()), logContext,
                "Response from sending insolvency data", logMap);

    }

    private <T> T mapToInsolvencyDelta(ChsDelta payload, Class<T> deltaClass)
            throws NonRetryableErrorException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(payload.getData(), deltaClass);
        } catch (Exception exception) {
            throw new NonRetryableErrorException(
                    "Error when mapping to insolvency delta class" + deltaClass.getName(),
                    exception);
        }
    }

    private void handleResponse(
            final ResponseStatusException ex,
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
            logger.errorContext(logContext, msg + ", retry", null, logMap);
            throw new RetryableErrorException(String
                    .format("Unsuccessful PUT API response, %s", msg));
        } else {
            logger.trace("Got success response from PUT insolvency");
        }
    }

}
