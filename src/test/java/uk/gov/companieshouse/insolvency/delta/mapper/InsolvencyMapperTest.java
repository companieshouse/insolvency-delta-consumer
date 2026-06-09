package uk.gov.companieshouse.insolvency.delta.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.insolvency.CaseDates;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.api.insolvency.ModelCase;
import uk.gov.companieshouse.api.insolvency.PractitionerAddress;
import uk.gov.companieshouse.api.insolvency.Practitioners;
import uk.gov.companieshouse.insolvency.delta.config.TestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        InsolvencyMapperImpl.class,
        CaseMapperImpl.class,
        PractitionersMapperImpl.class,
        PractitionerAddressMapperImpl.class,
        TestConfig.class}) // contains EncoderUtil bean
class InsolvencyMapperTest {

    private ObjectMapper mapper;
    private Insolvency insolvency;
    private InternalCompanyInsolvency insolvencyTarget;

    @Autowired
    InsolvencyMapper insolvencyMapper;

    @Autowired
    EncoderUtil encoderUtil;

    @BeforeEach
    void setUp() throws IOException {
        mapper = new ObjectMapper();

        String path = "insolvency-delta-complex.json";
        String input =
                FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        final InsolvencyDelta insolvencyDelta = mapper.readValue(input, InsolvencyDelta.class);
        insolvency = insolvencyDelta.getInsolvency().getFirst();

        String companyNumber = insolvency.getCompanyNumber();
        insolvencyTarget = insolvencyMapper.insolvencyDeltaToApi(insolvency, companyNumber);
    }

    @Test
    void shouldMapInternalData() {
        InternalData internalData = insolvencyTarget.getInternalData();

        // Nanosecond must be 9 digits and therefore padded with 000 at the end
        OffsetDateTime expectedDeltaAt = OffsetDateTime.of(2021, 10, 8, 15, 28, 23, 383176000, ZoneOffset.of("Z"));

        assertThat(insolvencyTarget).isNotNull();
        assertThat(internalData.getDeltaAt()).isEqualTo(expectedDeltaAt);
    }

    @Test
    void shouldMapFirstCase() {
        CompanyInsolvency externalData = insolvencyTarget.getExternalData();
        assertThat(externalData.getCases()).hasSize(4);

        ModelCase firstCase = externalData.getCases().getFirst();
        assertThat(firstCase.getNotes()).containsExactly("scottish-insolvency-info");
        assertThat(firstCase.getNumber()).isEqualTo("1");
        assertThat(firstCase.getType()).isEqualTo(ModelCase.TypeEnum.ADMINISTRATIVE_RECEIVER);
        assertThat(firstCase.getPractitioners()).hasSize(2);

        // FIRST PRACTITIONER
        Practitioners firstPractitioner = firstCase.getPractitioners().getFirst();
        assertThat(firstPractitioner.getName()).isEqualTo("Michael Horrocks");
        assertThat(firstPractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2004, 11, 22));
        assertThat(firstPractitioner.getCeasedToActOn()).isEqualTo(LocalDate.of(2006, 5, 5));
        assertThat(firstPractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.PRACTITIONER);
        verifyAddress(firstPractitioner.getAddress(), "Free From Gluten", "101 Barbirolli Square",
                "Lower Mosley Street", "Manchester", null, "M2 3PW");

        // SECOND PRACTITIONER
        Practitioners secondPractitioner = firstCase.getPractitioners().get(1);
        assertThat(secondPractitioner.getName()).isEqualTo("Craig Livesey");
        assertThat(secondPractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2004, 11, 22));
        assertThat(secondPractitioner.getCeasedToActOn()).isEqualTo(LocalDate.of(2006, 5, 5));
        assertThat(secondPractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.PRACTITIONER);
        verifyAddress(secondPractitioner.getAddress(), "Free From Debt", "101 Barbirolli Square",
                "Lower Mosley Street", "Liverpool", null, "L2 4PO");

        // DATES & LINKS
        assertThat(firstCase.getDates()).hasSize(1);
        List<CaseDates> caseDates = firstCase.getDates();
        assertThat(caseDates.getFirst().getType()).isEqualTo(CaseDates.TypeEnum.INSTRUMENTED_ON);
        assertThat(caseDates.getFirst().getDate()).isEqualTo(LocalDate.of(2002, 12, 17));

        assertThat(firstCase.getLinks()).isNotNull();
        String expectedMortgageIdEncoded = "cQjCn3ZZwOYqiJoMSc5Tm1OhFsE";
        String companyNumber = insolvency.getCompanyNumber();
        String expectedChargeLink = "/company/" + companyNumber
                + "/charges/" + expectedMortgageIdEncoded;
        assertThat(firstCase.getLinks().getCharge()).isEqualTo(expectedChargeLink);
    }

    @Test
    void shouldMapSecondCase() {
        CompanyInsolvency externalData = insolvencyTarget.getExternalData();
        ModelCase secondCase = externalData.getCases().get(1);

        assertThat(secondCase.getNotes()).containsExactly("scottish-insolvency-info");
        assertThat(secondCase.getNumber()).isEqualTo("2");
        assertThat(secondCase.getType()).isEqualTo(ModelCase.TypeEnum.CREDITORS_VOLUNTARY_LIQUIDATION);
        assertThat(secondCase.getPractitioners()).hasSize(1);

        Practitioners secondCasePractitioner = secondCase.getPractitioners().getFirst();
        assertThat(secondCasePractitioner.getName()).isEqualTo("Mark Terence Getliffe");
        assertThat(secondCasePractitioner.getAppointedOn()).isEqualTo(LocalDate.of(2005, 9, 29));
        assertThat(secondCasePractitioner.getRole()).isEqualTo(Practitioners.RoleEnum.RECEIVER);
        verifyAddress(secondCasePractitioner.getAddress(), "78 Gluten Tag Road", null,
                "Manchester", null, null, "M2 4WU");

        assertThat(secondCase.getDates()).hasSize(2);
        List<CaseDates> secondCaseDates = secondCase.getDates();
        assertThat(secondCaseDates.getFirst().getType()).isEqualTo(CaseDates.TypeEnum.WOUND_UP_ON);
        assertThat(secondCaseDates.getFirst().getDate()).isEqualTo(LocalDate.of(2005, 9, 29));
        assertThat(secondCaseDates.get(1).getType()).isEqualTo(CaseDates.TypeEnum.DISSOLVED_ON);
        assertThat(secondCaseDates.get(1).getDate()).isEqualTo(LocalDate.of(2021, 10, 1));

        assertThat(secondCase.getLinks()).isNull();
    }

    @Test
    void shouldMapThirdAndFourthCaseWithEmptyNotes() {
        CompanyInsolvency externalData = insolvencyTarget.getExternalData();

        ModelCase thirdCase = externalData.getCases().get(2);
        assertThat(thirdCase.getNotes()).isNull();

        ModelCase fourthCase = externalData.getCases().get(3);
        assertThat(fourthCase.getNotes()).isNull();
    }

    private void verifyAddress(PractitionerAddress address, String line1, String line2,
                               String locality, String region, String country, String postalCode) {
        assertThat(address.getAddressLine1()).isEqualTo(line1);
        assertThat(address.getAddressLine2()).isEqualTo(line2);
        assertThat(address.getLocality()).isEqualTo(locality);
        assertThat(address.getRegion()).isEqualTo(region);
        assertThat(address.getCountry()).isEqualTo(country);
        assertThat(address.getPostalCode()).isEqualTo(postalCode);
    }

}
