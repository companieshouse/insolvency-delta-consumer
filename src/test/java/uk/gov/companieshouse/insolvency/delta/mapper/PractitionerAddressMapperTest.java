package uk.gov.companieshouse.insolvency.delta.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.api.delta.PractitionerAddress;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { PractitionerAddressMapperImpl.class })
class PractitionerAddressMapperTest {

    @Autowired
    PractitionerAddressMapper mapper;

    @Test
    void shouldMapFullPractitionerAddress() {
        PractitionerAddress sourcePractitionerAddress = new PractitionerAddress();
        sourcePractitionerAddress.setAddressLine1("Yerrill Murphy Edelman House");
        sourcePractitionerAddress.setAddressLine2("1238 High Road");
        sourcePractitionerAddress.setLocality("Whetstone");
        sourcePractitionerAddress.setRegion("London");
        sourcePractitionerAddress.setCountry("");
        sourcePractitionerAddress.setPostalCode("N20 0LH");

        uk.gov.companieshouse.api.insolvency.PractitionerAddress targetAddress =
                mapper.map(sourcePractitionerAddress);

        assertThat(targetAddress).isNotNull();
        assertThat(targetAddress.getAddressLine1()).isEqualTo("Yerrill Murphy Edelman House");
        assertThat(targetAddress.getAddressLine2()).isEqualTo("1238 High Road");
        assertThat(targetAddress.getLocality()).isEqualTo("Whetstone");
        assertThat(targetAddress.getRegion()).isEqualTo("London");
        assertThat(targetAddress.getCountry()).isNull();
        assertThat(targetAddress.getPostalCode()).isEqualTo("N20 0LH");
    }

    @Test
    void shouldMapPractitionerAddressWithSubsetEmptyStrings() {
        PractitionerAddress sourcePractitionerAddress = new PractitionerAddress();
        sourcePractitionerAddress.setAddressLine1("Yerrill Murphy Edelman House");
        sourcePractitionerAddress.setAddressLine2("");
        sourcePractitionerAddress.setLocality("");
        sourcePractitionerAddress.setRegion("");
        sourcePractitionerAddress.setCountry("United Kingdom");
        sourcePractitionerAddress.setPostalCode("N20 0LH");

        uk.gov.companieshouse.api.insolvency.PractitionerAddress targetAddress =
                mapper.map(sourcePractitionerAddress);

        assertThat(targetAddress).isNotNull();
        assertThat(targetAddress.getAddressLine1()).isEqualTo("Yerrill Murphy Edelman House");
        assertThat(targetAddress.getAddressLine2()).isNull();
        assertThat(targetAddress.getLocality()).isNull();
        assertThat(targetAddress.getRegion()).isNull();
        assertThat(targetAddress.getCountry()).isEqualTo("United Kingdom");
        assertThat(targetAddress.getPostalCode()).isEqualTo("N20 0LH");
    }

    @Test
    void shouldMapPractitionerAddressWithMandatoryFields() {
        PractitionerAddress sourcePractitionerAddress = new PractitionerAddress();
        sourcePractitionerAddress.setAddressLine1("Yerrill Murphy Edelman House");
        sourcePractitionerAddress.setAddressLine2("");
        sourcePractitionerAddress.setLocality("");
        sourcePractitionerAddress.setRegion("");
        sourcePractitionerAddress.setCountry("");
        sourcePractitionerAddress.setPostalCode("N20 0LH");

        uk.gov.companieshouse.api.insolvency.PractitionerAddress targetAddress =
                mapper.map(sourcePractitionerAddress);

        assertThat(targetAddress).isNotNull();
        assertThat(targetAddress.getAddressLine1()).isEqualTo("Yerrill Murphy Edelman House");
        assertThat(targetAddress.getPostalCode()).isEqualTo("N20 0LH");
        assertThat(targetAddress.getAddressLine2()).isNull();
        assertThat(targetAddress.getLocality()).isNull();
        assertThat(targetAddress.getRegion()).isNull();
        assertThat(targetAddress.getCountry()).isNull();
    }
}
