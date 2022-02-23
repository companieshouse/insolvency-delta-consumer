package uk.gov.companieshouse.insolvency.delta.mapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.insolvency.CaseDates;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.ModelCase;
import uk.gov.companieshouse.api.insolvency.PractitionerAddress;
import uk.gov.companieshouse.api.insolvency.Practitioners;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class InsolvencyMapperTest {
    private ObjectMapper mapper;
    private Insolvency insolvency;

    @BeforeEach
    public void setUp() throws IOException {
        mapper = new ObjectMapper();

        String path = "insolvency-delta-complex.json";
        String input =
                FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        final InsolvencyDelta insolvencyDelta = mapper.readValue(input, InsolvencyDelta.class);
        insolvency = insolvencyDelta.getInsolvency().get(0);
    }

    @Test
    void shouldMapInsolvencyToCompanyInsolvency() {
        String companyNumber = insolvency.getCompanyNumber();
        InternalCompanyInsolvency insolvencyTarget =
                InsolvencyMapper.INSTANCE.insolvencyDeltaToApi(insolvency, companyNumber);

        assertThat(insolvency).isNotNull();
        assertThat(insolvencyTarget).isNotNull();
        assertThat(insolvencyTarget.getDeltaAt()).isEqualTo("20211008152823383176");
        assertThat(insolvencyTarget.getCases().size()).isEqualTo(2);

        // FIRST CASE
        ModelCase firstCase = insolvencyTarget.getCases().get(0);
        assertThat(firstCase.getNumber()).isEqualTo(1);
        assertThat(firstCase.getType()).isEqualTo(ModelCase.TypeEnum.ADMINISTRATIVE_RECEIVER);
        assertThat(firstCase.getPractitioners().size()).isEqualTo(2);

        // FIRST PRACTITIONER
        Practitioners firstPractitioner = firstCase.getPractitioners().get(0);
        assertThat(firstPractitioner.getName()).isEqualTo("Michael Horrocks");
        assertThat(firstPractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2004, 11, 22));
        assertThat(firstPractitioner.getCeasedToActOn()).isEqualTo(LocalDate.of(2006, 5, 5));
        assertThat(firstPractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.PRACTITIONER);

        PractitionerAddress firstAddress = firstPractitioner.getAddress();
        assertThat(firstAddress.getAddressLine1()).isEqualTo("Free From Gluten");
        assertThat(firstAddress.getAddressLine2()).isEqualTo("101 Barbirolli Square");
        assertThat(firstAddress.getLocality()).isEqualTo("Lower Mosley Street");
        assertThat(firstAddress.getRegion()).isEqualTo("Manchester");
        assertThat(firstAddress.getCountry()).isNull();
        assertThat(firstAddress.getPostalCode()).isEqualTo("M2 3PW");

        // SECOND PRACTITIONER
        Practitioners secondPractitioner = firstCase.getPractitioners().get(1);
        assertThat(secondPractitioner.getName()).isEqualTo("Craig Livesey");
        assertThat(secondPractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2004, 11, 22));
        assertThat(secondPractitioner.getCeasedToActOn()).isEqualTo(LocalDate.of(2006, 5, 5));
        assertThat(secondPractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.PRACTITIONER);

        PractitionerAddress secondAddress = secondPractitioner.getAddress();
        assertThat(secondAddress.getAddressLine1()).isEqualTo("Free From Debt");
        assertThat(secondAddress.getAddressLine2()).isEqualTo("101 Barbirolli Square");
        assertThat(secondAddress.getLocality()).isEqualTo("Lower Mosley Street");
        assertThat(secondAddress.getRegion()).isEqualTo("Liverpool");
        assertThat(secondAddress.getCountry()).isNull();
        assertThat(secondAddress.getPostalCode()).isEqualTo("L2 4PO");

        assertThat(firstCase.getDates().size()).isEqualTo(1);
        List<CaseDates> caseDates = firstCase.getDates();
        assertThat(caseDates.get(0).getType()).isEqualTo(CaseDates.TypeEnum.INSTRUMENTED_ON);
        assertThat(caseDates.get(0).getDate()).isEqualTo(LocalDate.of(2002, 12, 17));

        assertThat(firstCase.getLinks()).isNotNull();
        String mortgageId = insolvency.getCaseNumbers().get(0).getMortgageId().toString();
        String expectedChargeLink = "/company/" + companyNumber + "/charges/" + mortgageId;
        assertThat(firstCase.getLinks().getCharge()).isEqualTo(expectedChargeLink);

        // SECOND CASE
        ModelCase secondCase = insolvencyTarget.getCases().get(1);
        assertThat(secondCase.getNumber()).isEqualTo(2);
        assertThat(secondCase.getType()).isEqualTo(ModelCase.TypeEnum.CREDITORS_VOLUNTARY_LIQUIDATION);
        assertThat(secondCase.getPractitioners().size()).isEqualTo(1);

        Practitioners secondCasePractitioner = secondCase.getPractitioners().get(0);
        assertThat(secondCasePractitioner.getName()).isEqualTo("Mark Terence Getliffe");
        assertThat(secondCasePractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2005, 9, 29));
        assertThat(secondCasePractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.RECEIVER);

        PractitionerAddress secondCaseAddress = secondCasePractitioner.getAddress();
        assertThat(secondCaseAddress.getAddressLine1()).isEqualTo("78 Gluten Tag Road");
        assertThat(secondCaseAddress.getAddressLine2()).isNull();
        assertThat(secondCaseAddress.getLocality()).isEqualTo("Manchester");
        assertThat(secondCaseAddress.getRegion()).isNull();
        assertThat(secondCaseAddress.getCountry()).isNull();
        assertThat(secondCaseAddress.getPostalCode()).isEqualTo("M2 4WU");

        assertThat(secondCase.getDates().size()).isEqualTo(2);
        List<CaseDates> secondCaseDates = secondCase.getDates();
        assertThat(secondCaseDates.get(0).getType()).isEqualTo(CaseDates.TypeEnum.WOUND_UP_ON);
        assertThat(secondCaseDates.get(0).getDate()).isEqualTo(LocalDate.of(2005, 9, 29));
        assertThat(secondCaseDates.get(1).getType()).isEqualTo(CaseDates.TypeEnum.DISSOLVED_ON);
        assertThat(secondCaseDates.get(1).getDate()).isEqualTo(LocalDate.of(2021, 10, 01));

        assertThat(secondCase.getLinks()).isNull();
    }
}
