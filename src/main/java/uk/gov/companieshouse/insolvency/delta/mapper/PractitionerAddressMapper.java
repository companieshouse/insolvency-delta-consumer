package uk.gov.companieshouse.insolvency.delta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.api.delta.PractitionerAddress;

@Mapper
public interface PractitionerAddressMapper {
    PractitionerAddressMapper INSTANCE = Mappers.getMapper(PractitionerAddressMapper.class);

    uk.gov.companieshouse.api.insolvency.PractitionerAddress map(
            PractitionerAddress sourcePractitionerAddress);
}
