package uk.gov.companieshouse.insolvency.delta.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.AbstractIntegrationTest;
import uk.gov.companieshouse.insolvency.delta.config.KafkaTestContainerConfig;

public class InsolvencyDeltaConsumerITest extends AbstractIntegrationTest {

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${insolvency.delta.topic}")
    private String mainTopic;

    @Test
    public void testSendingKafkaMessage() {
        ChsDelta chsDelta = new ChsDelta("{ \"key\": \"value\" }", 1, "some_id");
        kafkaTemplate.send(mainTopic, chsDelta);
    }

}
