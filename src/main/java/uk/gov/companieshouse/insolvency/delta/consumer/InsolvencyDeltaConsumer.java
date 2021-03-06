package uk.gov.companieshouse.insolvency.delta.consumer;

import static java.lang.String.format;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
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

    /**
     * Default constructor.
     */
    @Autowired
    public InsolvencyDeltaConsumer(InsolvencyDeltaProcessor deltaProcessor, Logger logger) {
        this.deltaProcessor = deltaProcessor;
        this.logger = logger;
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
        Instant startTime = Instant.now();
        ChsDelta chsDelta = message.getPayload();
        String contextId = chsDelta.getContextId();
        logger.info(format("A new message successfully picked up from topic: %s, "
                        + "partition: %s and offset: %s with contextId: %s",
                topic, partition, offset, contextId));

        try {
            if (Boolean.TRUE.equals(chsDelta.getIsDelete())) {
                deltaProcessor.processDelete(message);
                logger.info(format("Insolvency Delete message with contextId: %s is successfully "
                                + "processed in %d milliseconds", contextId,
                        Duration.between(startTime, Instant.now()).toMillis()));
            } else {
                deltaProcessor.processDelta(message, topic, partition, offset);
                logger.info(format("Insolvency Delta message with contextId: %s is successfully "
                                + "processed in %d milliseconds", contextId,
                        Duration.between(startTime, Instant.now()).toMillis()));
            }
        } catch (Exception exception) {
            logger.errorContext(contextId, format("Exception occurred while processing "
                    + "message on the topic: %s", topic), exception, null);
            throw exception;
        }
    }

}
