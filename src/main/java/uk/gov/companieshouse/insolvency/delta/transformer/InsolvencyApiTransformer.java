package uk.gov.companieshouse.insolvency.delta.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.delta.mapper.InsolvencyMapper;

@Component
public class InsolvencyApiTransformer {

    /**
     * Transforms an Insolvency object from an InsolvencyDelta object
     * into an InternalCompanyInsolvency using mapstruct.
     * @param insolvency source object
     * @return source object mapped to InternalCompanyInsolvency
     */
    public InternalCompanyInsolvency transform(Insolvency insolvency) {
        String companyNumber = insolvency.getCompanyNumber();
        InternalCompanyInsolvency transformedInsolvency =
                InsolvencyMapper.INSTANCE.insolvencyDeltaToApi(insolvency, companyNumber);

        return transformedInsolvency;
    }
}
