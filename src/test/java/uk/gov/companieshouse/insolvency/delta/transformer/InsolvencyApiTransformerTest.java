package uk.gov.companieshouse.insolvency.delta.transformer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InsolvencyApiTransformerTest {

    private final InsolvencyApiTransformer transformer = new InsolvencyApiTransformer();

    @Test
    public void transformSuccessfully() {
        final String input = "test";
        assertThat(transformer.transform(input)).isEqualTo(input);
    }

}
