package uk.gov.companieshouse.insolvency.delta.processor;

import static uk.gov.companieshouse.insolvency.delta.InsolvencyDeltaConsumerApplication.APPLICATION_NAME_SPACE;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDeleteDelta;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.apiclient.InsolvencyApiClient;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;
import uk.gov.companieshouse.insolvency.delta.transformer.InsolvencyApiTransformer;
import uk.gov.companieshouse.insolvency.delta.validation.InsolvencyDeltaValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class InsolvencyDeltaProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final InsolvencyApiTransformer transformer;
    private final InsolvencyDeltaValidator validator;
    private final InsolvencyApiClient insolvencyApiClient;

    public InsolvencyDeltaProcessor(InsolvencyApiTransformer transformer, InsolvencyDeltaValidator validator,
            InsolvencyApiClient insolvencyApiClient) {
        this.transformer = transformer;
        this.validator = validator;
        this.insolvencyApiClient = insolvencyApiClient;
    }

    /**
     * Process CHS Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta, String topic, String partition, String offset) {
        LOGGER.info("Extracting insolvency delta from payload", DataMapHolder.getLogMap());
        ChsDelta payload = chsDelta.getPayload();
        InsolvencyDelta insolvencyDelta = mapToInsolvencyDelta(payload, InsolvencyDelta.class);
        Insolvency insolvency = insolvencyDelta.getInsolvency().get(0);

        final String companyNumber = insolvency.getCompanyNumber();
        DataMapHolder.get().companyNumber(companyNumber);

        try {
            validator.validateCaseDates(insolvency);
        } catch (Exception ex) {
            final String msg = "Error when validating case dates";
            LOGGER.error(msg, ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(msg, ex);
        }

        LOGGER.info("Transforming delta", DataMapHolder.getLogMap());
        InternalCompanyInsolvency internalCompanyInsolvency = transformer.transform(insolvency);

        final String updatedBy = String.format("%s-%s-%s", topic, partition, offset);
        internalCompanyInsolvency.getInternalData().setUpdatedBy(updatedBy);

        LOGGER.info("Sending PUT request to Insolvency Data API", DataMapHolder.getLogMap());
        insolvencyApiClient.putInsolvency(companyNumber, internalCompanyInsolvency);
    }

    /**
     * Process Insolvency Delete messages.
     */
    public void processDelete(Message<ChsDelta> chsDelta) {
        LOGGER.info("Extracting insolvency delta from payload", DataMapHolder.getLogMap());
        ChsDelta payload = chsDelta.getPayload();
        InsolvencyDeleteDelta insolvencyDeleteDelta = mapToInsolvencyDelta(payload, InsolvencyDeleteDelta.class);

        final String companyNumber = insolvencyDeleteDelta.getCompanyNumber();
        DataMapHolder.get().companyNumber(companyNumber);

        LOGGER.info("Sending DELETE request to Insolvency Data API", DataMapHolder.getLogMap());
        insolvencyApiClient.deleteInsolvency(companyNumber, insolvencyDeleteDelta.getDeltaAt());
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
}
