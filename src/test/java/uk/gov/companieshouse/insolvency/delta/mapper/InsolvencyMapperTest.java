package uk.gov.companieshouse.insolvency.delta.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.insolvency.CaseDates;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class InsolvencyMapperTest {
    private ObjectMapper mapper;
    private Insolvency insolvency;

    @BeforeEach
    public void setUp() throws IOException {
        mapper = new ObjectMapper();

        String path = "insolvency-delta-example.json";
        String input = FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        final InsolvencyDelta insolvencyDelta = mapper.readValue(input, InsolvencyDelta.class);
        insolvency = insolvencyDelta.getInsolvency().get(0);
    }

    @Test
    public void shouldMapInsolvencyToCompanyInsolvency() {
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
        InternalCompanyInsolvency insolvencyTarget = InsolvencyMapper.INSTANCE.insolvencyDeltaToApi(insolvency);

        assertThat(insolvency).isNotNull();
        assertThat(insolvencyTarget).isNotNull();
        assertThat(insolvencyTarget.getDeltaAt()).isEqualTo("20211008152823383176");
        assertThat(insolvencyTarget.getCases().get(0).getNumber()).isEqualTo(1);
        assertThat(insolvencyTarget.getCases().get(0).getPractitioners().get(0).getName()).isEqualTo("Bernard Hoffman");
        assertThat(insolvencyTarget.getCases().get(0).getPractitioners().get(0).getAppointedOn()).isEqualTo(LocalDate.of(2020, 05, 06));
        assertThat(insolvencyTarget.getCases().get(0).getDates().size()).isEqualTo(2);
        assertThat(insolvencyTarget.getCases().get(0).getDates().get(0).getDate()).isEqualTo(LocalDate.of(2020,05,06));
        assertThat(insolvencyTarget.getCases().get(0).getDates().get(0).getType()).isEqualTo(CaseDates.TypeEnum.WOUND_UP_ON);
        assertThat(insolvencyTarget.getCases().get(0).getDates().get(1).getDate()).isEqualTo(LocalDate.of(2020,04,29));
        assertThat(insolvencyTarget.getCases().get(0).getDates().get(1).getType()).isEqualTo(CaseDates.TypeEnum.DECLARATION_SOLVENT_ON);

    }
}
