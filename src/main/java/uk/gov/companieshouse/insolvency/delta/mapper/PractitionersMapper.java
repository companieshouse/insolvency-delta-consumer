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
            @ValueMapping(source = "NUMBER_1", target = "PRACTITIONER"),
            @ValueMapping(source = "NUMBER_2", target = "PROVISIONAL_LIQUIDATOR"),
            @ValueMapping(source = "NUMBER_3", target = "INTERIM_LIQUIDATOR"),
            @ValueMapping(source = "NUMBER_4", target = "FINAL_LIQUIDATOR"),
            @ValueMapping(source = "NUMBER_5", target = "RECEIVER"),
            @ValueMapping(source = "NUMBER_6", target = "ADMINISTRATIVE_RECEIVER"),
            @ValueMapping(source = "NUMBER_7", target = "RECEIVER_MANAGER"),
            @ValueMapping(source = "NUMBER_8", target = "PROPOSED_LIQUIDATOR")})
    Practitioners map(Appointment sourcePractitioner);

    /**
     * Concatenates practitioner name to one string.
     * @param practitioners target practitioner.
     * @param sourcePractitioner source practitioner.
     */
    @AfterMapping
    default void setPractitionerName(
            @MappingTarget Practitioners practitioners, Appointment sourcePractitioner) {
        String joined =
                Stream.of(sourcePractitioner.getForename(),
                            sourcePractitioner.getMiddleName(),
                            sourcePractitioner.getSurname())
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        practitioners.setName(joined);
    }
}
