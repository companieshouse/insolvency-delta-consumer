package uk.gov.companieshouse.insolvency.delta.mapper;

import org.apache.commons.lang.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.api.delta.PractitionerAddress;

@Mapper(componentModel = "spring")
public interface PractitionerAddressMapper {

    @Mapping(target = "addressLine2",
            expression = "java(emptyStringToNull(sourcePractitionerAddress.getAddressLine2()))")
    @Mapping(target = "locality",
            expression = "java(emptyStringToNull(sourcePractitionerAddress.getLocality()))")
    @Mapping(target = "region",
            expression = "java(emptyStringToNull(sourcePractitionerAddress.getRegion()))")
    @Mapping(target = "country",
            expression = "java(emptyStringToNull(sourcePractitionerAddress.getCountry()))")
    uk.gov.companieshouse.api.insolvency.PractitionerAddress map(
            PractitionerAddress sourcePractitionerAddress);

    /**
     * By default we always get the optional properties on the source address as "" (empty string).
     * This method excludes them from being added to the target object.
     * @param property practitioner address property
     * @return the property itself if not an empty string, null otherwise
     */
    default String emptyStringToNull(String property) {
        return StringUtils.isBlank(property) ? null : property;
    }
}
