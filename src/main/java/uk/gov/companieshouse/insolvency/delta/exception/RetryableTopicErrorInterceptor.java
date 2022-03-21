package uk.gov.companieshouse.insolvency.delta.exception;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;

import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;

public class RetryableTopicErrorInterceptor implements ProducerInterceptor<String, Object> {

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
        String nextTopic = record.topic().contains("-error") ? getNextErrorTopic(record) : record.topic();
        if (nextTopic.contains("-invalid")) {
            return new ProducerRecord<>(nextTopic, record.key(), record.value());
        }

        return record;
    }

    private String getNextErrorTopic(ProducerRecord<String, Object> record) {
        Header header = record.headers().lastHeader(EXCEPTION_CAUSE_FQCN);
        return header != null &&
                new String(header.value()).contains(NonRetryableErrorException.class.getName()) ?
                record.topic().replace("-error", "-invalid") : record.topic();
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}
