package uk.gov.companieshouse.insolvency.delta.mapper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.api.insolvency.ModelCase;


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

    /**
     * Invoked at the end of the auto-generated mapping methods and
     * if the jurisdiction is equal to 3 and case type is different than
     * (ADMINISTRATION_ORDER or IN_ADMINISTRATION), then set notes
     * with scottish-insolvency-info.
     *
     * @param companyInsolvency     the target companyInsolvency object
     * @param insolvency     the target insolvency object
     */
    @AfterMapping
    default void  setNotes(@MappingTarget CompanyInsolvency companyInsolvency,
                           Insolvency insolvency) {

        if ("3".equalsIgnoreCase(insolvency.getJurisdiction())) {
            companyInsolvency.getCases().forEach(modelCase -> {
                if (!ModelCase.TypeEnum.ADMINISTRATION_ORDER.equals(modelCase.getType())
                        && !ModelCase.TypeEnum.IN_ADMINISTRATION.equals(modelCase.getType())) {
                    modelCase.setNotes(Arrays.asList("scottish-insolvency-info"));
                }
            });

        }
    }

}
