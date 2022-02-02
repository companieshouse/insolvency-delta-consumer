package uk.gov.companieshouse.insolvency.delta.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.insolvency.delta.AbstractKafkaContainerTest;

@SpringBootTest
@DirtiesContext
//@Import(AbstractKafkaContainerTest.KafkaTestContainerConfig.class)
@ActiveProfiles({"test"})
public class InsolvencyDeltaConsumerITest extends AbstractKafkaContainerTest {

    @Autowired
    public KafkaTemplate<String, String> kafkaTemplate;

    @Value("${insolvency.delta.topic.main}")
    private String mainTopic;

    @Test
    public void returnHealthStatusSuccessfully() {
        // kafkaTemplate.send(mainTopic, "some test message");
    }
}
