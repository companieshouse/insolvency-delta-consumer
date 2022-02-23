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

        String path = "insolvency-delta-example.json";
        String input =
                FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        final InsolvencyDelta insolvencyDelta = mapper.readValue(input, InsolvencyDelta.class);
        insolvency = insolvencyDelta.getInsolvency().get(0);
    }

    @Test
    void shouldMapInsolvencyToCompanyInsolvency() {
//        Insolvency insolvency = new Insolvency();
//        insolvency.setDeltaAt("20211008152823383176");
//
//        CaseNumber sourceCase = new CaseNumber();
//        sourceCase.setCaseNumber(1);
//        sourceCase.setCaseType(CaseNumber.CaseTypeEnum.RECEIVER_MANAGER);
//        sourceCase.setCaseTypeId(CaseNumber.CaseTypeIdEnum.NUMBER_5);
//        sourceCase.mortgageId(Long.valueOf("3001368176"));
//
//        Appointment sourcePractitioner = new Appointment();
//        sourcePractitioner.setForename("Gilbert");
//        sourcePractitioner.setMiddleName("J");
//        sourcePractitioner.setSurname("Lemon");
//        sourcePractitioner.setApptType(Appointment.ApptTypeEnum.NUMBER_7);
//        sourcePractitioner.setApptDate("20200430");
//        sourcePractitioner.setCeasedToActAppt("20200401");
//
//        PractitionerAddress sourcePractitionerAddress = new PractitionerAddress();
//        sourcePractitionerAddress.setAddressLine1("3rd Floor 9 Colmore Row");
//        sourcePractitionerAddress.setLocality("Birmingham");
//        sourcePractitionerAddress.setPostalCode("B3 2BJ");
//
//        sourcePractitioner.setPractitionerAddress(sourcePractitionerAddress);
//        sourceCase.addAppointmentsItem(sourcePractitioner);
//        insolvency.addCaseNumbersItem(sourceCase);
//
        String companyNumber = insolvency.getCompanyNumber();
        InternalCompanyInsolvency insolvencyTarget =
                InsolvencyMapper.INSTANCE.insolvencyDeltaToApi(insolvency, companyNumber);

        assertThat(insolvency).isNotNull();
        assertThat(insolvencyTarget).isNotNull();
        assertThat(insolvencyTarget.getDeltaAt()).isEqualTo("20211008152823383176");

        assertThat(insolvencyTarget.getCases().size()).isEqualTo(1);
        ModelCase firstCase = insolvencyTarget.getCases().get(0);
        assertThat(firstCase.getNumber()).isEqualTo(1);
        assertThat(firstCase.getType()).isEqualTo(ModelCase.TypeEnum.MEMBERS_VOLUNTARY_LIQUIDATION);
        assertThat(firstCase.getPractitioners().size()).isEqualTo(1);


        Practitioners firstPractitioner = firstCase.getPractitioners().get(0);
        assertThat(firstPractitioner.getName()).isEqualTo("Bernard Hoffman");
        assertThat(firstPractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2020, 05, 06));
        assertThat(firstPractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.PRACTITIONER);

        PractitionerAddress address = firstPractitioner.getAddress();
        assertThat(address.getAddressLine1()).isEqualTo("Yerrill Murphy Edelman House");
        assertThat(address.getAddressLine2()).isEqualTo("1238 High Road");
        assertThat(address.getLocality()).isEqualTo("Whetstone");
        assertThat(address.getRegion()).isEqualTo("London");
        assertThat(address.getPostalCode()).isEqualTo("N20 0LH");

        assertThat(firstCase.getDates().size()).isEqualTo(2);
        List<CaseDates> caseDates = firstCase.getDates();
        assertThat(caseDates.get(0).getType()).isEqualTo(CaseDates.TypeEnum.WOUND_UP_ON);
        assertThat(caseDates.get(0).getDate()).isEqualTo(LocalDate.of(2020, 05, 06));
        assertThat(caseDates.get(1).getType()).isEqualTo(CaseDates.TypeEnum.DECLARATION_SOLVENT_ON);
        assertThat(caseDates.get(1).getDate()).isEqualTo(LocalDate.of(2020, 04, 29));

        assertThat(firstCase.getLinks()).isNotNull();
        String mortgageId = insolvency.getCaseNumbers().get(0).getMortgageId().toString();
        String expectedChargeLink = "/company/" + companyNumber + "/charges/" + mortgageId;
        assertThat(firstCase.getLinks().getCharge()).isEqualTo(expectedChargeLink);
    }
}
