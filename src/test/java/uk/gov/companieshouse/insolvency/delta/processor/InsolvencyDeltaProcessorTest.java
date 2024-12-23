package uk.gov.companieshouse.insolvency.delta.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
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
import uk.gov.companieshouse.api.delta.Appointment;
import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.delta.PractitionerAddress;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.apiclient.InsolvencyApiClient;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.transformer.InsolvencyApiTransformer;
import uk.gov.companieshouse.insolvency.delta.validation.InsolvencyDeltaValidator;

@ExtendWith(MockitoExtension.class)
class InsolvencyDeltaProcessorTest {

    private InsolvencyDeltaProcessor deltaProcessor;

    @Mock
    private InsolvencyDeltaValidator validator;

    @Mock
    private InsolvencyApiTransformer transformer;

    @Mock
    private InsolvencyApiClient insolvencyApiClient;

    @BeforeEach
    void setUp() {
        deltaProcessor = new InsolvencyDeltaProcessor(transformer, validator, insolvencyApiClient);
    }

    @Test
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into an InsolvencyDelta")
    void When_ValidChsDeltaMessage_Expect_ValidInsolvencyDeltaMapping() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(false);
        InsolvencyDelta expectedInsolvencyDelta = createInsolvencyDelta();
        Insolvency expectedInsolvency = expectedInsolvencyDelta.getInsolvency().get(0);
        when(transformer.transform(expectedInsolvency)).thenReturn(internalCompanyInsolvencyMock());

        deltaProcessor.processDelta(mockChsDeltaMessage, "topic", "partition", "offset");

        verify(transformer).transform(expectedInsolvency);
        verify(insolvencyApiClient).putInsolvency("02588581", internalCompanyInsolvencyMock());
    }

    @Test
    @DisplayName("When mapping an invalid ChsDelta message into Insolvency Delta then throws a non-retryable exception")
    void When_CantTransformIntoInsolvencyDelta_nonRetryableError() {
        Message<ChsDelta> invalidChsDeltaMessage = invalidChsDeltaMessage(false);
        assertThrows(NonRetryableErrorException.class,
                () -> deltaProcessor.processDelta(invalidChsDeltaMessage, "topic", "partition", "offset"));
    }

    @Test
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into an InsolvencyDeleteDelta")
    void When_ValidChsDeltaMessage_Expect_ValidInsolvencyDeleteDeltaMapping() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(true);

        deltaProcessor.processDelete(mockChsDeltaMessage);

        verify(insolvencyApiClient).deleteInsolvency("12345678", "20230724093435661593");
    }

    @Test
    @DisplayName("When mapping an invalid ChsDelta message into Insolvency Delete Delta then throws a non-retryable exception")
    void When_CantTransformIntoInsolvencyDeltaApi_nonRetryableError() {
        Message<ChsDelta> invalidChsDeltaMessage = invalidChsDeltaMessage(true);
        assertThrows(NonRetryableErrorException.class, () -> deltaProcessor.processDelete(invalidChsDeltaMessage));
    }

    private InternalCompanyInsolvency internalCompanyInsolvencyMock() {
        InternalCompanyInsolvency internalInsolvency = new InternalCompanyInsolvency();
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("topic-partition-offset");
        internalInsolvency.setInternalData(internalData);
        internalInsolvency.setExternalData(new CompanyInsolvency());
        return internalInsolvency;
    }

    private Message<ChsDelta> createChsDeltaMessage(boolean isDelete) throws IOException {
        var payloadFilename = (isDelete)
                ? "insolvency-delete-delta.json"
                : "insolvency-delta-example.json";
        InputStreamReader exampleInsolvencyJsonPayload = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(payloadFilename)));
        String insolvencyData = FileCopyUtils.copyToString(exampleInsolvencyJsonPayload);

        ChsDelta mockChsDelta = ChsDelta.newBuilder()
                .setData(insolvencyData)
                .setContextId("context_id")
                .setAttempt(1)
                .setIsDelete(isDelete)
                .build();

        return MessageBuilder
                .withPayload(mockChsDelta)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "topic")
                .setHeader("INSOLVENCY_DELTA_RETRY_COUNT", 1)
                .setHeader(KafkaHeaders.RECEIVED_PARTITION, "partition")
                .setHeader(KafkaHeaders.OFFSET, "offset")
                .build();
    }

    private Message<ChsDelta> invalidChsDeltaMessage(boolean isDelete) {
        String insolvencyData = "Invalid Insolvency Data";

        ChsDelta mockChsDelta = ChsDelta.newBuilder()
                .setData(insolvencyData)
                .setContextId("context_id")
                .setAttempt(1)
                .setIsDelete(isDelete)
                .build();

        return MessageBuilder
                .withPayload(mockChsDelta)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "topic")
                .setHeader("INSOLVENCY_DELTA_RETRY_COUNT", 1)
                .setHeader(KafkaHeaders.RECEIVED_PARTITION, "partition")
                .setHeader(KafkaHeaders.OFFSET, "offset")
                .build();
    }

    private InsolvencyDelta createInsolvencyDelta() {
        Appointment appointment = getAppointment();

        CaseNumber caseNumber = new CaseNumber();
        caseNumber.setCaseNumber(1);
        caseNumber.setCaseType(CaseNumber.CaseTypeEnum.MEMBERS_VOLUNTARY_LIQUIDATION);
        caseNumber.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_1);
        caseNumber.setSwornDate("20200429");
        caseNumber.windUpDate("20200506");
        caseNumber.setMortgageId(Long.valueOf("3001368176"));
        caseNumber.addAppointmentsItem(appointment);

        Insolvency insolvency = new Insolvency();
        insolvency.setDeltaAt("20211008152823383176");
        insolvency.setCompanyNumber("02588581");
        insolvency.addCaseNumbersItem(caseNumber);

        return new InsolvencyDelta().addInsolvencyItem(insolvency);
    }

    private static Appointment getAppointment() {
        PractitionerAddress address = new PractitionerAddress();
        address.setAddressLine1("Yerrill Murphy Edelman House");
        address.setAddressLine2("1238 High Road");
        address.setLocality("Whetstone");
        address.setRegion("London");
        address.setPostalCode("N20 0LH");
        address.setCountry("");

        Appointment appointment = new Appointment();
        appointment.setForename("Bernard");
        appointment.setSurname("Hoffman");
        appointment.setApptType(Appointment.ApptTypeEnum.NUMBER_1);
        appointment.setApptDate("20200506");
        appointment.setPractitionerAddress(address);
        return appointment;
    }
}
