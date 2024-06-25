package uk.gov.companieshouse.insolvency.delta.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.mapper.InsolvencyMapper;
import uk.gov.companieshouse.insolvency.delta.mapper.InsolvencyStatusMapper;

@Component
public class InsolvencyApiTransformer {

    private final InsolvencyMapper mapper;
    private final InsolvencyStatusMapper insolvencyStatusMapper;

    @Autowired
    public InsolvencyApiTransformer(InsolvencyMapper mapper,
                                    InsolvencyStatusMapper insolvencyStatusMapper) {
        this.mapper = mapper;
        this.insolvencyStatusMapper = insolvencyStatusMapper;
    }

    /**
     * Transforms an Insolvency object from an InsolvencyDelta object
     * into an InternalCompanyInsolvency using mapstruct.
     *
     * @param insolvency source object
     * @return source object mapped to InternalCompanyInsolvency
     */
    public InternalCompanyInsolvency transform(Insolvency insolvency)
            throws RetryableErrorException {
        try {
            String companyNumber =
                    insolvency.getCompanyNumber();

            InternalCompanyInsolvency internalCompanyInsolvency =
                    mapper.insolvencyDeltaToApi(insolvency, companyNumber);

            internalCompanyInsolvency
                    .getExternalData()
                    .setStatus(insolvencyStatusMapper.mapStatus(insolvency.getStatus()));

            return internalCompanyInsolvency;
        } catch (Exception exception) {
            throw new RetryableErrorException("Unable to map to Insolvency API object",
                    exception);
        }
    }
}
