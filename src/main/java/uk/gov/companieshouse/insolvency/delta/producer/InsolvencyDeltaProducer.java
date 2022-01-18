package uk.gov.companieshouse.insolvency.delta.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InsolvencyDeltaProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public InsolvencyDeltaProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send(String topic, String payload) {
    kafkaTemplate.send(topic, payload);
  }

}
