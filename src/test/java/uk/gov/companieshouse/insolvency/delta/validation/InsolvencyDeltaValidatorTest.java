package uk.gov.companieshouse.insolvency.delta.validation;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;

@ExtendWith(MockitoExtension.class)
public class InsolvencyDeltaValidatorTest {

    InsolvencyDeltaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InsolvencyDeltaValidator();
    }

    @Test
    void When_ExtraDateFieldPresent_ForCaseType2_Then_nonRetryableError_isThrown() throws IOException {
        Insolvency insolvency = new Insolvency();
        CaseNumber caseNumber = new CaseNumber();
        caseNumber.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_2);
        caseNumber.setSwornDate("20200429");
        insolvency.addCaseNumbersItem(caseNumber);
        Assertions.assertThrows(NonRetryableErrorException.class, () -> validator.validateCaseDates(insolvency));
    }

    @Test
    void When_NoExtraDateFieldPresent_ForCaseType2_Then_noError_isThrown() throws IOException {
        Insolvency insolvency = new Insolvency();
        CaseNumber caseNumber = new CaseNumber();
        caseNumber.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_2);
        insolvency.addCaseNumbersItem(caseNumber);
        Assertions.assertDoesNotThrow(() -> validator.validateCaseDates(insolvency));
    }

    @Test
    void When_CaseTypeNo8_Missing_Mandatory_Field() throws IOException {
        Insolvency insolvency = new Insolvency();
        CaseNumber caseNumber = new CaseNumber();
        caseNumber.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_8);
        insolvency.addCaseNumbersItem(caseNumber);
        Assertions.assertThrows(NonRetryableErrorException.class, () -> validator.validateCaseDates(insolvency));
    }

    @Test
    void When_CaseTypeNo17_Contains_Mandatory_Field() throws IOException {
        Insolvency insolvency = new Insolvency();
        CaseNumber caseNumber = new CaseNumber();
        caseNumber.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_17);
        caseNumber.setAppointmentDate("202220512");
        insolvency.addCaseNumbersItem(caseNumber);
        Assertions.assertDoesNotThrow(() -> validator.validateCaseDates(insolvency));    
    }
}
