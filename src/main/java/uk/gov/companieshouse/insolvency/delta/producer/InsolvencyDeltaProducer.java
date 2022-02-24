package uk.gov.companieshouse.insolvency.delta.producer;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.logging.Logger;

@Component
public class InsolvencyDeltaProducer {

    private final Logger logger;

    private final CHKafkaProducer chKafkaProducer;

    @Autowired
    public InsolvencyDeltaProducer(Logger logger,
                                   CHKafkaProducer chKafkaProducer) {
        this.logger = logger;
        this.chKafkaProducer = chKafkaProducer;
    }

    /**
     * Send Kafka message.
     */
    public void send(Message message) {
        try {
            chKafkaProducer.send(message);
        } catch (ExecutionException | InterruptedException ex) {
            logger.error("Error while sending the message", ex);
        }
    }

}
