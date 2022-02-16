package uk.gov.companieshouse.insolvency.delta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

@Mapper(uses = {CaseMapper.class})
public interface InsolvencyMapper {
    InsolvencyMapper INSTANCE = Mappers.getMapper(InsolvencyMapper.class);

    @Mapping(target = "etag", ignore = true) // doesn't exist on source
    @Mapping(target = "status", ignore = true) // TODO doesn't seem to exist on source - confirm
    @Mapping(target = "cases", source = "caseNumbers")
    InternalCompanyInsolvency insolvencyDeltaToApi(Insolvency insolvency);
    // each InsolvencyDelta contains a List<Insolvency>, and each of these in turn contains:
    // companyNumber, deltaAt, List<CaseNumber>

}
