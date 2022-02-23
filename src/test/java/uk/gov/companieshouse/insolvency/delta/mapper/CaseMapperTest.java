package uk.gov.companieshouse.insolvency.delta.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.companieshouse.api.delta.CaseNumber.CaseTypeEnum.ADMINISTRATION;
import static uk.gov.companieshouse.api.delta.CaseNumber.CaseTypeEnum.RECEIVER_MANAGER;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.ADMINISTRATION_STARTED_ON;
import static uk.gov.companieshouse.api.insolvency.ModelCase.TypeEnum.ADMINISTRATION_ORDER;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.insolvency.CaseDates;
import uk.gov.companieshouse.api.insolvency.ModelCase;

public class CaseMapperTest {

    String companyNumber = "1232466";
    String mortgageId = "1232466";

    @Test
    void shouldMapFullCaseWithDatesAndMortgageId() {
        CaseNumber sourceCase = new CaseNumber();
        sourceCase.setCaseNumber(1);
        sourceCase.setCaseType(ADMINISTRATION);
        sourceCase.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_7);
        sourceCase.setMortgageId((Long.valueOf(mortgageId)));
        sourceCase.setAdminOrderDate("20010705");
        sourceCase.setDischargeAdminOrderDate("20210823");

        ModelCase targetCase = CaseMapper.INSTANCE.map(sourceCase, companyNumber);

        assertThat(targetCase.getType()).isEqualTo(ADMINISTRATION_ORDER);
        assertThat(targetCase.getNumber()).isEqualTo(1);
        assertThat(targetCase.getNotes()).isNull();

        List<CaseDates> caseDates = targetCase.getDates();
        assertThat(caseDates.size()).isEqualTo(2);
        assertThat(caseDates.get(0).getType()).isEqualTo(ADMINISTRATION_STARTED_ON);
        assertThat(caseDates.get(0).getDate()).isEqualTo(LocalDate.of(2001, 7, 5));
        assertThat(caseDates.get(1).getType()).isEqualTo(CaseDates.TypeEnum.ADMINISTRATION_DISCHARGED_ON);
        assertThat(caseDates.get(1).getDate()).isEqualTo(LocalDate.of(2021, 8, 23));

        String expectedChargeLink = "/company/" + companyNumber + "/charges/" + mortgageId;
        assertThat(targetCase.getLinks().getCharge()).isEqualTo(expectedChargeLink);
    }

    @Test
    void shouldMapCaseWithoutDatesAndMortgageId() {
        CaseNumber sourceCase = new CaseNumber();
        sourceCase.setCaseNumber(1);
        sourceCase.setCaseType(RECEIVER_MANAGER);
        sourceCase.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_5);

        ModelCase targetCase = CaseMapper.INSTANCE.map(sourceCase, companyNumber);

        assertThat(targetCase.getType()).isEqualTo(ModelCase.TypeEnum.RECEIVER_MANAGER);
        assertThat(targetCase.getNumber()).isEqualTo(1);
        assertThat(targetCase.getDates().size()).isEqualTo(0);
        assertThat(targetCase.getPractitioners()).isNull();
        assertThat(targetCase.getLinks()).isNull();
    }
}
