package uk.gov.companieshouse.insolvency.delta.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class InsolvencyMapperTest {
    @Test
    public void shouldMapInsolvencyToCompanyInsolvency() {
        Insolvency insolvency = new Insolvency();
        insolvency.setDeltaAt("20211008152823383176");
        CaseNumber sourceCase = new CaseNumber();
        sourceCase.setCaseNumber(2);
        insolvency.addCaseNumbersItem(sourceCase);

        InternalCompanyInsolvency insolvencyTarget = InsolvencyMapper.INSTANCE.insolvencyDeltaToApi(insolvency);

        assertThat(insolvencyTarget).isNotNull();
        assertThat(insolvencyTarget.getDeltaAt()).isEqualTo("20211008152823383176");
        assertThat(insolvencyTarget.getCases().get(0).getNumber()).isEqualTo(2);
    }
}
