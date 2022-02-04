package uk.gov.companieshouse.insolvency.delta.transformer;

import org.junit.Test;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;

import static org.assertj.core.api.Assertions.assertThat;

public class InsolvencyApiTransformerTest {

    private final InsolvencyApiTransformer transformer = new InsolvencyApiTransformer();

    @Test
    public void transformSuccessfully() {
        final InsolvencyDelta input = new InsolvencyDelta();
        assertThat(transformer.transform(input)).isEqualTo(input);
    }

}
