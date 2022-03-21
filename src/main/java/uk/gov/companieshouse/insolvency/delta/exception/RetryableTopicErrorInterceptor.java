package uk.gov.companieshouse.insolvency.delta.exception;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;

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
        Header header = record.headers().lastHeader("kafka_exception-fqcn");
        return header != null &&
                new String(header.value()).contains("delta.exception.NonRetryableErrorException") ?
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
