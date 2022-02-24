package uk.gov.companieshouse.insolvency.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.producer.InsolvencyDeltaProducer;
import uk.gov.companieshouse.insolvency.delta.transformer.InsolvencyApiTransformer;
import uk.gov.companieshouse.logging.Logger;

@Component
public class InsolvencyDeltaProcessor {

    private final InsolvencyDeltaProducer deltaProducer;
    private final InsolvencyApiTransformer transformer;
    private final Logger logger;

    /**
     * The constructor.
     */
    @Autowired
    public InsolvencyDeltaProcessor(InsolvencyDeltaProducer deltaProducer,
                                    InsolvencyApiTransformer transformer,
                                    Logger logger) {
        this.deltaProducer = deltaProducer;
        this.transformer = transformer;
        this.logger = logger;
    }

    /**
     * Process CHS Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        try {
            MessageHeaders headers = chsDelta.getHeaders();
            final String receivedTopic =
                    Objects.requireNonNull(headers.get(KafkaHeaders.RECEIVED_TOPIC)).toString();
            final ChsDelta payload = chsDelta.getPayload();

            ObjectMapper mapper = new ObjectMapper();
            InsolvencyDelta insolvencyDelta = mapper.readValue(payload.getData(),
                    InsolvencyDelta.class);

            transformer.transform(insolvencyDelta);
        } catch (RetryableErrorException ex) {
            retryDeltaMessage(chsDelta);
        } catch (Exception ex) {
            handleErrorMessage(chsDelta);
            // send to error topic
        }
    }

    public void retryDeltaMessage(Message<ChsDelta> chsDelta) {

    }

    private void handleErrorMessage(Message<ChsDelta> chsDelta) {

    }

}
