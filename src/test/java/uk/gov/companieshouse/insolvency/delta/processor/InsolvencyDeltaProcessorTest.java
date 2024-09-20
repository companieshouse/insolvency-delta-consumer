package uk.gov.companieshouse.insolvency.delta.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.service.api.ApiClientService;
import uk.gov.companieshouse.insolvency.delta.transformer.InsolvencyApiTransformer;
import uk.gov.companieshouse.insolvency.delta.validation.InsolvencyDeltaValidator;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class InsolvencyDeltaProcessorTest {

    private InsolvencyDeltaProcessor deltaProcessor;

    @Mock
    InsolvencyDeltaValidator validator;

    @Mock
    private InsolvencyApiTransformer transformer;

    @Mock
    private InternalCompanyInsolvency internalCompanyInsolvency;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        deltaProcessor = new InsolvencyDeltaProcessor(apiClientService, transformer, logger, validator);
    }

    @Test
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into an InsolvencyDelta")
    void When_ValidChsDeltaMessage_Expect_ValidInsolvencyDeltaMapping() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(false);
        InsolvencyDelta expectedInsolvencyDelta = createInsolvencyDelta();
        Insolvency expectedInsolvency = expectedInsolvencyDelta.getInsolvency().get(0);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        when(transformer.transform(expectedInsolvency)).thenReturn(internalCompanyInsolvencyMock());
        when(apiClientService.putInsolvency(eq("context_id"),eq("02588581"), eq(internalCompanyInsolvencyMock()))).thenReturn(response);
        deltaProcessor.processDelta(mockChsDeltaMessage, "topic", "partition", "offset");

        verify(apiClientService).putInsolvency("context_id", "02588581", internalCompanyInsolvencyMock());
        verify(transformer).transform(expectedInsolvency);
    }

    @Test
    @DisplayName("Bad request when calling put insolvency, throws non retryable error")
    void When_PutInsolvencyBadRequest_NonRetryableError() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(false);
        InsolvencyDelta expectedInsolvencyDelta = createInsolvencyDelta();
        Insolvency expectedInsolvency = expectedInsolvencyDelta.getInsolvency().get(0);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), null, null);

        when(transformer.transform(expectedInsolvency)).thenReturn(internalCompanyInsolvencyMock());
        when(apiClientService.putInsolvency(eq("context_id"),eq("02588581"), eq(internalCompanyInsolvencyMock()))).thenReturn(response);
        assertThrows(NonRetryableErrorException.class, () -> deltaProcessor.processDelta(mockChsDeltaMessage, "topic", "partition", "offset"));
        verify(apiClientService).putInsolvency("context_id", "02588581", internalCompanyInsolvencyMock());
        verify(transformer).transform(expectedInsolvency);
    }

    @Test
    @DisplayName("Getting another 4xx when calling put insolvency, throws retryable error")
    void When_PutInsolvencyUnauthorized_RetryableError() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(false);
        InsolvencyDelta expectedInsolvencyDelta = createInsolvencyDelta();
        Insolvency expectedInsolvency = expectedInsolvencyDelta.getInsolvency().get(0);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), null, null);

        when(transformer.transform(expectedInsolvency)).thenReturn(internalCompanyInsolvencyMock());
        when(apiClientService.putInsolvency(eq("context_id"),eq("02588581"), eq(internalCompanyInsolvencyMock()))).thenReturn(response);
        assertThrows(RetryableErrorException.class, () -> deltaProcessor.processDelta(mockChsDeltaMessage, "topic", "partition", "offset"));
        verify(apiClientService).putInsolvency("context_id", "02588581", internalCompanyInsolvencyMock());
        verify(transformer).transform(expectedInsolvency);
    }

    @Test
    @DisplayName("Getting internal server error when calling put insolvency, throws retryable error")
    void When_PutInsolvencyInternalServerError_RetryableError() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(false);
        InsolvencyDelta expectedInsolvencyDelta = createInsolvencyDelta();
        Insolvency expectedInsolvency = expectedInsolvencyDelta.getInsolvency().get(0);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null);

        when(transformer.transform(expectedInsolvency)).thenReturn(internalCompanyInsolvencyMock());
        when(apiClientService.putInsolvency(eq("context_id"),eq("02588581"), eq(internalCompanyInsolvencyMock()))).thenReturn(response);
        assertThrows(RetryableErrorException.class, () -> deltaProcessor.processDelta(mockChsDeltaMessage, "topic", "partition", "offset"));
        verify(apiClientService).putInsolvency("context_id", "02588581", internalCompanyInsolvencyMock());
        verify(transformer).transform(expectedInsolvency);
    }

    @Test
    @DisplayName("When mapping an invalid ChsDelta message into Insolvency Delta then throws a non-retryable exception")
    void When_CantTransformIntoInsolvencyDelta_nonRetryableError() throws IOException {
        Message<ChsDelta> invalidChsDeltaMessage = invalidChsDeltaMessage(false);
        assertThrows(NonRetryableErrorException.class, () -> deltaProcessor.processDelta(invalidChsDeltaMessage, "topic", "partition", "offset"));
    }

    @Test
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into an InsolvencyDeleteDelta")
    void When_ValidChsDeltaMessage_Expect_ValidInsolvencyDeleteDeltaMapping() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(true);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(apiClientService.deleteInsolvency("context_id", "12345678")).thenReturn(response);

        deltaProcessor.processDelete(mockChsDeltaMessage);

        verify(apiClientService).deleteInsolvency("context_id", "12345678");
    }

    @Test
    @DisplayName("When mapping an invalid ChsDelta message into Insolvency Delete Delta then throws a non-retryable exception")
    void When_CantTransformIntoInsolvencyDeltaApi_nonRetryableError() throws IOException {
        Message<ChsDelta> invalidChsDeltaMessage = invalidChsDeltaMessage(true);
        assertThrows(NonRetryableErrorException.class, () -> deltaProcessor.processDelete(invalidChsDeltaMessage));
    }

    @ParameterizedTest
    @MethodSource("provideExceptionParameters")
    @DisplayName("When calling PUT insolvency and an error occurs then throw the appropriate exception based on the error type")
    void shouldThrowAppropriateExceptionDuringPutRequestWhenReceivingHttpErrorStatus(HttpStatus httpStatus, Class<Throwable> exception) throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(false);
        final ApiResponse<Void> errorResponse = new ApiResponse<>(httpStatus.value(), null, null);

        when(transformer.transform(any())).thenReturn(internalCompanyInsolvency);
        when(internalCompanyInsolvency.getInternalData()).thenReturn(new InternalData());
        when(apiClientService.putInsolvency(anyString(), anyString(), any())).thenReturn(errorResponse);

        Executable executable = () -> deltaProcessor.processDelta(mockChsDeltaMessage, "topic", "partition", "offset");

        assertThrows(exception, executable);
    }

    @ParameterizedTest
    @MethodSource("provideExceptionParameters")
    @DisplayName("When calling DELETE insolvency and an error occurs then throw the appropriate exception based on the error type")
    void When_DeleteInsolvencyException_throw_appropriate_exception(HttpStatus httpStatus, Class<Throwable> exception) throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage(true);
        final ApiResponse<Void> errorResponse = new ApiResponse<>(httpStatus.value(), null, null);

        when(apiClientService.deleteInsolvency("context_id", "12345678")).thenReturn(errorResponse);

        assertThrows(exception, () -> deltaProcessor.processDelete(mockChsDeltaMessage));

        verify(apiClientService).deleteInsolvency("context_id", "12345678");
    }

    private static Stream<Arguments> provideExceptionParameters() {
        return Stream.of(
                Arguments.of(HttpStatus.BAD_REQUEST, NonRetryableErrorException.class),
                Arguments.of(HttpStatus.CONFLICT , NonRetryableErrorException.class),
                Arguments.of(HttpStatus.NOT_FOUND, RetryableErrorException.class),
                Arguments.of(HttpStatus.UNAUTHORIZED, RetryableErrorException.class),
                Arguments.of(HttpStatus.SERVICE_UNAVAILABLE, RetryableErrorException.class),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, RetryableErrorException.class)
        );
    }

    private InternalCompanyInsolvency internalCompanyInsolvencyMock() {
        InternalCompanyInsolvency internalCompanyInsolvency = new InternalCompanyInsolvency();
        InternalData internalData = new InternalData();
        internalData.setUpdatedBy("topic-partition-offset");
        internalCompanyInsolvency.setInternalData(internalData);
        internalCompanyInsolvency.setExternalData(new CompanyInsolvency());
        return internalCompanyInsolvency;
    }

    private Message<ChsDelta> createChsDeltaMessage(boolean isDelete) throws IOException {
        var payloadFilename = (isDelete)
                ? "insolvency-delete-delta.json"
                : "insolvency-delta-example.json";
        InputStreamReader exampleInsolvencyJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(payloadFilename));
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
                .setHeader(KafkaHeaders.RECEIVED_PARTITION_ID, "partition")
                .setHeader(KafkaHeaders.OFFSET, "offset")
                .build();
    }

    private Message<ChsDelta> invalidChsDeltaMessage(boolean isDelete) throws IOException {
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
                .setHeader(KafkaHeaders.RECEIVED_PARTITION_ID, "partition")
                .setHeader(KafkaHeaders.OFFSET, "offset")
                .build();
    }

    private InsolvencyDelta createInsolvencyDelta() {
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
}
