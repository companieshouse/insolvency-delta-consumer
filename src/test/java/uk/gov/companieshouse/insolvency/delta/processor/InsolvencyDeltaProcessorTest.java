package uk.gov.companieshouse.insolvency.delta.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.producer.InsolvencyDeltaProducer;

@ExtendWith(MockitoExtension.class)
public class InsolvencyDeltaProcessorTest {

    private InsolvencyDeltaProcessor deltaProcessor;

    @Mock
    private InsolvencyDeltaProducer insolvencyDeltaProducer;

    @BeforeEach
    void setUp() {
        deltaProcessor = new InsolvencyDeltaProcessor(insolvencyDeltaProducer);
    }

    @Test
    void processDeltaSuccessful() throws JsonProcessingException {
        ChsDelta mockChsDelta = ChsDelta.newBuilder().setData("test").setContextId("context_id").setAttempt(1).build();
        Message<ChsDelta> chsDelta = MessageBuilder.withPayload(mockChsDelta).setHeader("foo", "bar").build();

        deltaProcessor.processDelta(chsDelta);
    }
}
