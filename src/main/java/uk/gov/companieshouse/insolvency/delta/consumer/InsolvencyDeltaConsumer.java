package uk.gov.companieshouse.insolvency.delta.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.RetryTopicHeaders;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.processor.InsolvencyDeltaProcessor;


@Component
public class InsolvencyDeltaConsumer {

    private final InsolvencyDeltaProcessor deltaProcessor;

    /**
     * Default constructor.
     */
    @Autowired
    public InsolvencyDeltaConsumer(InsolvencyDeltaProcessor deltaProcessor) {
        this.deltaProcessor = deltaProcessor;
    }

    /**
     * Receives Main topic messages.
     */
    @RetryableTopic(attempts = "${insolvency.delta.attempts}",
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            backoff = @Backoff(delayExpression = "${insolvency.delta.backoff-delay}"),
            retryTopicSuffix = "-${insolvency.delta.group-id}-retry",
            dltTopicSuffix = "-${insolvency.delta.group-id}-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(topics = "${insolvency.delta.topic}",
            groupId = "${insolvency.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> message,
            @Header(name = RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS, required = false) Integer attempt,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) String offset) {
        ChsDelta chsDelta = message.getPayload();
        if (Boolean.TRUE.equals(chsDelta.getIsDelete())) {
            deltaProcessor.processDelete(message);
        } else {
            deltaProcessor.processDelta(message, topic, partition, offset);
        }
    }

}
