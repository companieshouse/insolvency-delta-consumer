package uk.gov.companieshouse.insolvency.delta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.insolvency.ModelCase;

@Mapper
public interface CaseMapper {
    CaseMapper INSTANCE = Mappers.getMapper(CaseMapper.class);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "dates", ignore = true)
    @Mapping(target = "practitioners", ignore = true)
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "notes", ignore = true) // doesn't exist on source
    @Mapping(target = "number", source = "caseNumber")
    ModelCase map(CaseNumber sourceCase);
}
