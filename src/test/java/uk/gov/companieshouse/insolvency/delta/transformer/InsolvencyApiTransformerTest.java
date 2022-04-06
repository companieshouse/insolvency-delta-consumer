package uk.gov.companieshouse.insolvency.delta.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.mapper.InsolvencyMapper;

@ExtendWith(MockitoExtension.class)
class InsolvencyApiTransformerTest {

    private static final String COMPANY_NUMBER = "01876743";

    @Mock
    private InsolvencyMapper mapper;

    private InsolvencyApiTransformer transformer;

    @BeforeEach
    public void setup() {
        transformer = new InsolvencyApiTransformer(mapper);
    }

    @Test
    @DisplayName("Successfully invokes the mapstruct mapper")
    void When_TransformerIsCalled_ThenMapperIsSuccessfullyInvoked() {
        Insolvency insolvency = new Insolvency();
        insolvency.setCompanyNumber(COMPANY_NUMBER);
        InternalCompanyInsolvency mockInternalInsolvency = mock(InternalCompanyInsolvency.class);

        when(mapper.insolvencyDeltaToApi(insolvency, COMPANY_NUMBER))
                .thenReturn(mockInternalInsolvency);

        InternalCompanyInsolvency actual = transformer.transform(insolvency);
        assertThat(actual).isEqualTo(mockInternalInsolvency);
    }

    @Test
    @DisplayName("Throws an error when there's an issue with the transformation")
    void When_ErrorDuringTransformation_ThenThrows() {
        Insolvency insolvency = new Insolvency();
        insolvency.setCompanyNumber(COMPANY_NUMBER);

        when(mapper.insolvencyDeltaToApi(insolvency, COMPANY_NUMBER))
                .thenThrow(NullPointerException.class);

        assertThrows(RetryableErrorException.class, () -> transformer.transform(insolvency));
    }
}
