package uk.gov.companieshouse.insolvency.delta.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.api.delta.Appointment;
import uk.gov.companieshouse.api.insolvency.Practitioners;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        PractitionersMapperImpl.class,
        PractitionerAddressMapperImpl.class})
class PractitionersMapperTest {

    @Autowired
    PractitionersMapper mapper;

    @Test
    void shouldMapPractitionerWithAllNames() {
        Appointment sourcePractitioner = new Appointment();
        sourcePractitioner.setForename("Diane");
        sourcePractitioner.setMiddleName("Elizabeth");
        sourcePractitioner.setSurname("Hill");
        sourcePractitioner.setApptType(Appointment.ApptTypeEnum.NUMBER_7);
        sourcePractitioner.setApptDate("20200430");
        sourcePractitioner.setCeasedToActAppt("20210205");

        Practitioners targetPractitioner = mapper.map(sourcePractitioner);

        assertThat(targetPractitioner.getName()).isEqualTo("Diane Elizabeth Hill");
        assertThat(targetPractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2020, 4, 30));
        assertThat(targetPractitioner.getCeasedToActOn()).isEqualTo(LocalDate.of(2021, 2, 5));
        assertThat(targetPractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.RECEIVER_MANAGER);
        assertThat(targetPractitioner.getAddress()).isNull();
    }

    @Test
    void shouldMapPractitionerWithoutAllNamesAndDates() {
        Appointment sourcePractitioner = new Appointment();
        sourcePractitioner.setForename("Hildegard");
        sourcePractitioner.setMiddleName("");
        sourcePractitioner.setSurname("Strudel");
        sourcePractitioner.setApptType(Appointment.ApptTypeEnum.NUMBER_1);
        sourcePractitioner.setApptDate("20200506");

        Practitioners targetPractitioner = mapper.map(sourcePractitioner);

        assertThat(targetPractitioner.getName()).isEqualTo("Hildegard Strudel");
        assertThat(targetPractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2020, 5, 6));
        assertThat(targetPractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.PRACTITIONER);
        assertThat(targetPractitioner.getCeasedToActOn()).isNull();
        assertThat(targetPractitioner.getAddress()).isNull();
    }
}
