package uk.gov.companieshouse.insolvency.delta.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.*;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.producer.InsolvencyDeltaProducer;
import uk.gov.companieshouse.insolvency.delta.transformer.InsolvencyApiTransformer;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InsolvencyDeltaProcessorTest {

    private InsolvencyDeltaProcessor deltaProcessor;

    @Mock
    private InsolvencyDeltaProducer insolvencyDeltaProducer;

    @Mock
    private InsolvencyApiTransformer transformer;

    @BeforeEach
    void setUp() {
        deltaProcessor = new InsolvencyDeltaProcessor(insolvencyDeltaProducer, transformer);
    }

    @Test
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into an InsolvencyDelta")
    void When_ValidChsDeltaMessage_Expect_ValidInsolvencyDeltaMapping() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage();
        InsolvencyDelta expectedInsolvencyDelta = createInsolvencyDelta();
        when(transformer.transform(expectedInsolvencyDelta)).thenCallRealMethod();

        deltaProcessor.processDelta(mockChsDeltaMessage);

        verify(transformer).transform(expectedInsolvencyDelta);
    }

    private Message<ChsDelta> createChsDeltaMessage() throws IOException {
        InputStreamReader exampleInsolvencyJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("insolvency-delta-example.json"));
        String insolvencyData = FileCopyUtils.copyToString(exampleInsolvencyJsonPayload);

        ChsDelta mockChsDelta = ChsDelta.newBuilder()
                .setData(insolvencyData)
                .setContextId("context_id")
                .setAttempt(1)
                .build();

        return MessageBuilder
                .withPayload(mockChsDelta)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "test")
                .setHeader("INSOLVENCY_DELTA_RETRY_COUNT", 1)
                .build();
    }

    private InsolvencyDelta createInsolvencyDelta() {
        PractitionerAddress address = new PractitionerAddress();
        address.setAddressLine1("Yerrill Murphy Edelman House");
        address.setAddressLine2("1238 High Road");
        address.setLocality("Whetstone");
        address.setRegion("London");
        address.setPostalCode("N20 0LH");

        Appointment appointment = new Appointment();
        appointment.setForename("Bernard");
        appointment.setSurname("Hoffman");
        appointment.setApptType(Appointment.ApptTypeEnum.NUMBER_1);
        appointment.setApptDate("20200506");
        appointment.setPractitionerAddress(address);

        CaseNumber caseNumber = new CaseNumber();
        caseNumber.setCaseNumber(1);
        caseNumber.setCaseType(CaseNumber.CaseTypeEnum.MEMBERS_VOLUNTARY_LIQUIDATION);
        caseNumber.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_1);
        caseNumber.setSwornDate("20200429");
        caseNumber.windUpDate("20200506");
        caseNumber.addAppointmentsItem(appointment);

        Insolvency insolvency = new Insolvency();
        insolvency.setDeltaAt("20211008152823383176");
        insolvency.setCompanyNumber("02588581");
        insolvency.addCaseNumbersItem(caseNumber);

        return new InsolvencyDelta().addInsolvencyItem(insolvency);
    }
}
