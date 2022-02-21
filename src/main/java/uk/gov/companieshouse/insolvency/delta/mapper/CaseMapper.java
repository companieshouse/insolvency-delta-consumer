package uk.gov.companieshouse.insolvency.delta.mapper;

import org.apache.tomcat.jni.Local;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.insolvency.CaseDates;
import uk.gov.companieshouse.api.insolvency.ModelCase;

import java.time.LocalDate;
import java.util.stream.Stream;

@Mapper(uses = {PractitionersMapper.class})
public interface CaseMapper {
    CaseMapper INSTANCE = Mappers.getMapper(CaseMapper.class);

    @Mapping(target = "dates", ignore = true)
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "notes", ignore = true) // doesn't exist on source
    @Mapping(target = "practitioners", source = "appointments")
    @Mapping(target = "number", source = "caseNumber")
    @Mapping(target = "type", source = "caseType")
    @ValueMappings({
            @ValueMapping(target = "MEMBERS_VOLUNTARY_LIQUIDATION", source = "MEMBERS_VOLUNTARY_LIQUIDATION"),
            @ValueMapping(target = "CREDITORS_VOLUNTARY_LIQUIDATION", source = "CREDITORS_VOLUNTARY_LIQUIDATION"),
            @ValueMapping(target = "COMPULSORY_LIQUIDATION", source = "COMPULSORY_LIQUIDATION"),
            @ValueMapping(target = "RECEIVER_MANAGER", source = "RECEIVER_MANAGER"),
            @ValueMapping(target = "ADMINISTRATIVE_RECEIVER", source = "ADMINISTRATIVE_RECEIVER"),
            @ValueMapping(target = "ADMINISTRATION_ORDER", source = "ADMINISTRATION"),
            @ValueMapping(target = "CORPORATE_VOLUNTARY_ARRANGEMENT", source = "CORPORATE_VOLUNTARY_ARRANGEMENT"),
            @ValueMapping(target = "CORPORATE_VOLUNTARY_ARRANGEMENT", source = "CORPORATE_VOLUNTARY_ARRANGEMENT_"),
            @ValueMapping(target = "IN_ADMINISTRATION", source = "IN_ADMINISTRATION"),
            @ValueMapping(target = "CORPORATE_VOLUNTARY_ARRANGEMENT_MORATORIUM", source = "CVA_MORATORIA"),
            @ValueMapping(target = "FOREIGN_INSOLVENCY", source = "FOREIGN_INSOLVENCY"),
            @ValueMapping(target = "RECEIVERSHIP", source = "MORATORIUM") // TODO: Wrong mapping
            })
    ModelCase map(CaseNumber sourceCase);

    @AfterMapping
    default void mapDateToCaseDates(@MappingTarget ModelCase modelCase, CaseNumber sourceDate) {
        CaseDates caseDates = new CaseDates();
        Stream.of(sourceDate.getWindUpDate())
                .forEach(date -> modelCase.addDatesItem(caseDates));

    }
}
