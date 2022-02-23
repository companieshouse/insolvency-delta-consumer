package uk.gov.companieshouse.insolvency.delta.mapper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.api.delta.Appointment;
import uk.gov.companieshouse.api.insolvency.Practitioners;


@Mapper(uses = {PractitionerAddressMapper.class})
public interface PractitionersMapper {
    PractitionersMapper INSTANCE = Mappers.getMapper(PractitionersMapper.class);

    @Mapping(target = "name", ignore = true)
    @Mapping(target = "address", source = "practitionerAddress")
    @Mapping(target = "appointedOn", source = "apptDate", dateFormat = "yyyyMMdd")
    @Mapping(target = "ceasedToActOn", source = "ceasedToActAppt", dateFormat = "yyyyMMdd")
    @Mapping(target = "role", source = "apptType")
    @ValueMappings({
            @ValueMapping(target = "PRACTITIONER", source = "NUMBER_1"),
            @ValueMapping(target = "PROVISIONAL_LIQUIDATOR", source = "NUMBER_2"),
            @ValueMapping(target = "INTERIM_LIQUIDATOR", source = "NUMBER_3"),
            @ValueMapping(target = "FINAL_LIQUIDATOR", source = "NUMBER_4"),
            @ValueMapping(target = "RECEIVER", source = "NUMBER_5"),
            @ValueMapping(target = "ADMINISTRATIVE_RECEIVER", source = "NUMBER_6"),
            @ValueMapping(target = "RECEIVER_MANAGER", source = "NUMBER_7"),
            @ValueMapping(target = "PROPOSED_LIQUIDATOR", source = "NUMBER_8")})
    Practitioners map(Appointment sourcePractitioner);

    /**
     * Concatenates practitioner names to a string.
     *
     * @param practitioners      target practitioner.
     * @param sourcePractitioner source practitioner.
     */
    @AfterMapping
    default void setPractitionerName(@MappingTarget Practitioners practitioners,
                                     Appointment sourcePractitioner) {
        String joinedPractitionerNames = Stream.of(sourcePractitioner.getForename(),
                        sourcePractitioner.getMiddleName(),
                        sourcePractitioner.getSurname())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(" "));

        practitioners.setName(joinedPractitionerNames);
    }
}
