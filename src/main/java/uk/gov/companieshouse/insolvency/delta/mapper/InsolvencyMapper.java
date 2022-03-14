package uk.gov.companieshouse.insolvency.delta.mapper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;


@Mapper(componentModel = "spring", uses = {CaseMapper.class})
public interface InsolvencyMapper {

    @Mapping(target = "externalData.etag", ignore = true) // doesn't exist on source
    @Mapping(target = "externalData.status", ignore = true) // doesn't exist on source
    @Mapping(target = "internalData.deltaAt", ignore = true)
    @Mapping(target = "externalData.cases", source = "caseNumbers")
    InternalCompanyInsolvency insolvencyDeltaToApi(Insolvency insolvency,
                                                   @Context String companyNumber);

    /**
     * Invoked at the end of the auto-generated mapping methods and parses deltaAt from
     * String to OffsetDateTime with UTC zone offset.
     *
     * @param internalData     the target InternalData object
     * @param insolvency    the source Insolvency object
     */
    @AfterMapping
    default void parseDeltaAt(@MappingTarget InternalData internalData,
                            Insolvency insolvency) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
                .withZone(ZoneId.of("Z"));
        ZonedDateTime datetime = ZonedDateTime.parse(insolvency.getDeltaAt(), formatter);
        internalData.setDeltaAt(datetime.toOffsetDateTime());
    }
}
