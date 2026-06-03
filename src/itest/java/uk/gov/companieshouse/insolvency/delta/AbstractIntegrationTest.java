package uk.gov.companieshouse.insolvency.delta;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.insolvency.delta.config.KafkaTestContainerConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(KafkaTestContainerConfig.class)
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest {

}
