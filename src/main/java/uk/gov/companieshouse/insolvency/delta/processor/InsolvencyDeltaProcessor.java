package uk.gov.companieshouse.insolvency.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.producer.InsolvencyDeltaProducer;
import uk.gov.companieshouse.insolvency.delta.service.api.ApiClientService;
import uk.gov.companieshouse.insolvency.delta.transformer.InsolvencyApiTransformer;
import uk.gov.companieshouse.logging.Logger;

@Component
public class InsolvencyDeltaProcessor {

    private final InsolvencyDeltaProducer deltaProducer;
    private final InsolvencyApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final uk.gov.companieshouse.logging.Logger logger;

    /**
     * The constructor.
     */
    @Autowired
    public InsolvencyDeltaProcessor(InsolvencyDeltaProducer deltaProducer,
                                    ApiClientService apiClientService,
                                    InsolvencyApiTransformer transformer,
                                    Logger logger) {
        this.deltaProducer = deltaProducer;
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Process CHS Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        try {
            MessageHeaders headers = chsDelta.getHeaders();
            final String logContext = chsDelta.getPayload().getContextId();
            final Map<String, Object> logMap = new HashMap<>();
            final String receivedTopic =
                    Objects.requireNonNull(headers.get(KafkaHeaders.RECEIVED_TOPIC)).toString();
            final ChsDelta payload = chsDelta.getPayload();

            ObjectMapper mapper = new ObjectMapper();
            InsolvencyDelta insolvencyDelta = mapper.readValue(payload.getData(),
                    InsolvencyDelta.class);
            Insolvency insolvency = insolvencyDelta.getInsolvency().get(0);
            InternalCompanyInsolvency internalCompanyInsolvency = transformer.transform(insolvency);
            final String companyNumber = insolvencyDelta.getInsolvency().get(0).getCompanyNumber();

            transformer.transform(insolvencyDelta);
        } catch (RetryableErrorException ex) {
            retryDeltaMessage(chsDelta);
        } catch (Exception ex) {
            handleErrorMessage(chsDelta);
            // send to error topic
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
            logger.errorContext(logContext, msg, null, logMap);
            throw new NonRetryableErrorException(msg);
        } else if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {
            // any other client or server status is retryable
            logger.errorContext(logContext, msg + ", retry", null, logMap);
            throw new RetryableErrorException(msg);
        } else {
            logger.debugContext(logContext, msg, logMap);
        }
    }

    public void retryDeltaMessage(Message<ChsDelta> chsDelta) {

    }

    private void handleErrorMessage(Message<ChsDelta> chsDelta) {

    }

}
