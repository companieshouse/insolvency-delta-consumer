package uk.gov.companieshouse.insolvency.delta.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.insolvency.delta.producer.InsolvencyDeltaProducer;


@Component
public class InsolvencyDeltaConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(InsolvencyDeltaConsumer.class);
  private final InsolvencyDeltaProducer deltaProducer;

  @Autowired
  public InsolvencyDeltaConsumer(InsolvencyDeltaProducer deltaProducer) {
    this.deltaProducer = deltaProducer;
  }

  @KafkaListener(topics = "${insolvency.delta.topic.main}")
  public void receiveMain(ConsumerRecord<?, String> consumerRecord) {
    LOGGER.info("<<ALERT>> new message on MAIN topic with payload: " + consumerRecord);
    deltaProducer.send("retry-topic", consumerRecord.value());
  }

  @KafkaListener(topics = "${insolvency.delta.topic.retry}")
  public void receiveRetry(ConsumerRecord<?, String> consumerRecord) {
    LOGGER.info("<<ALERT>> new message on RETRY topic with payload: " + consumerRecord);
    deltaProducer.send("error-topic", consumerRecord.value());
  }

  @KafkaListener(topics = "${insolvency.delta.topic.error}")
  public void receiveError(ConsumerRecord<?, String> consumerRecord) {
    LOGGER.info("<<ALERT>> new message on ERROR topic with payload: " + consumerRecord);
  }

}
