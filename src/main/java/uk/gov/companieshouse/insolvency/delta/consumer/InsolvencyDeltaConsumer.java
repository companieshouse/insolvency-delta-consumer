package uk.gov.companieshouse.insolvency.delta.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.processor.InsolvencyDeltaProcessor;


@Component
public class InsolvencyDeltaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsolvencyDeltaConsumer.class);

    private final InsolvencyDeltaProcessor deltaProcessor;

    @Autowired
    public InsolvencyDeltaConsumer(InsolvencyDeltaProcessor deltaProcessor) {
        this.deltaProcessor = deltaProcessor;
    }

    /**
     * Receives Main topic messages.
     */
    @KafkaListener(topics = "${insolvency.delta.topic.main}",
            groupId = "insolvency.delta.topic.main")
    @Retryable
    public void receiveMainMessages(Message<ChsDelta> chsDeltaMessage) {
        LOGGER.info("A new message read from MAIN topic with payload: "
                + chsDeltaMessage.getPayload());
        deltaProcessor.processDelta(chsDeltaMessage);
    }

    /**
     * Receives Retry topic messages.
     */
    @KafkaListener(topics = "${insolvency.delta.topic.retry}",
            groupId = "insolvency.delta.topic.retry")
    public void receiveRetryMessages(Message<ChsDelta> message) {
        LOGGER.info(String.format("A new message read from RETRY topic with payload:%s "
                + "and headers:%s ", message.getPayload(), message.getHeaders()));
        deltaProcessor.processDelta(message);
    }

}
