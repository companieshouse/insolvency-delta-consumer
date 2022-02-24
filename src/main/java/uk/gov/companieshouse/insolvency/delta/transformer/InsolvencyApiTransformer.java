package uk.gov.companieshouse.insolvency.delta.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.delta.mapper.InsolvencyMapper;

@Component
public class InsolvencyApiTransformer {

    private final InsolvencyMapper mapper;

    @Autowired
    public InsolvencyApiTransformer(InsolvencyMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Transforms an Insolvency object from an InsolvencyDelta object
     * into an InternalCompanyInsolvency using mapstruct.
     * @param insolvency source object
     * @return source object mapped to InternalCompanyInsolvency
     */
    public InternalCompanyInsolvency transform(Insolvency insolvency) {
        String companyNumber = insolvency.getCompanyNumber();
        return mapper.insolvencyDeltaToApi(insolvency, companyNumber);
    }
}
