package uk.gov.companieshouse.insolvency.delta.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.processor.InsolvencyDeltaProcessor;
import uk.gov.companieshouse.logging.Logger;


@Component
public class InsolvencyDeltaConsumer {

    private final InsolvencyDeltaProcessor deltaProcessor;
    private final Logger logger;
    public final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Default constructor.
     */
    @Autowired
    public InsolvencyDeltaConsumer(InsolvencyDeltaProcessor deltaProcessor, Logger logger,
                                   KafkaTemplate<String, Object> kafkaTemplate) {
        this.deltaProcessor = deltaProcessor;
        this.logger = logger;
        this.kafkaTemplate = kafkaTemplate;

    }

    /**
     * Receives Main topic messages.
     */
    @RetryableTopic(attempts = "${insolvency.delta.attempts}",
            backoff = @Backoff(delayExpression = "${insolvency.delta.backoff-delay}"),
            fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC,
            retryTopicSuffix = "-${insolvency.delta.group-id}-retry",
            dltTopicSuffix = "-${insolvency.delta.group-id}-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(topics = "${insolvency.delta.topic}",
            groupId = "${insolvency.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> message,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION_ID) String partition,
                                    @Header(KafkaHeaders.OFFSET) String offset) {
        logger.trace(String.format("A new message from %s topic with payload:%s "
                + "and headers:%s ", topic, message.getPayload(), message.getHeaders()));
        try {
            deltaProcessor.processDelta(message, topic, partition, offset);
        } catch (Exception exception) {
            logger.error(String.format("Exception occurred while processing the topic %s "
                    + "with message %s, exception thrown is %s", topic, message, exception));
            throw exception;
        }
    }

}
