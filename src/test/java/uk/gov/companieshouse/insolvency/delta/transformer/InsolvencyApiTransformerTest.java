package uk.gov.companieshouse.insolvency.delta.transformer;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

import static org.assertj.core.api.Assertions.assertThat;

public class InsolvencyApiTransformerTest {

    private final InsolvencyApiTransformer transformer = new InsolvencyApiTransformer();

    @Test
    public void transformSuccessfully() {
        final Insolvency input = new Insolvency();
        InternalCompanyInsolvency internalCompanyInsolvency = new InternalCompanyInsolvency();
        assertThat(transformer.transform(input)).isEqualTo(internalCompanyInsolvency);
    }
}
