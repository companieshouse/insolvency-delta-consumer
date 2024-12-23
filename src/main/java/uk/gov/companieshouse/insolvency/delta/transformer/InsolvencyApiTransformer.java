package uk.gov.companieshouse.insolvency.delta.transformer;

import static uk.gov.companieshouse.insolvency.delta.InsolvencyDeltaConsumerApplication.APPLICATION_NAME_SPACE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;
import uk.gov.companieshouse.insolvency.delta.mapper.InsolvencyMapper;
import uk.gov.companieshouse.insolvency.delta.mapper.InsolvencyStatusMapper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class InsolvencyApiTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

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
            LOGGER.info("Unable to map to Insolvency API object", DataMapHolder.getLogMap());
            throw new RetryableErrorException("Unable to map to Insolvency API object", exception);
        }
    }
}
