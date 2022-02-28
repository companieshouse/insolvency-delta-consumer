package uk.gov.companieshouse.insolvency.delta.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

@Mapper(componentModel = "spring", uses = {CaseMapper.class})
public interface InsolvencyMapper {

    @Mapping(target = "etag", ignore = true) // doesn't exist on source
    @Mapping(target = "status", ignore = true) // doesn't exist on source
    @Mapping(target = "deltaAt", source = "deltaAt")
    @Mapping(target = "cases", source = "caseNumbers")
    InternalCompanyInsolvency insolvencyDeltaToApi(Insolvency insolvency,
                                                   @Context String companyNumber);
}
