package uk.gov.companieshouse.insolvency.delta.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import org.springframework.util.Base64Utils;
import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.insolvency.CaseDates;
import uk.gov.companieshouse.api.insolvency.Links;
import uk.gov.companieshouse.api.insolvency.ModelCase;

@Mapper(componentModel = "spring", uses = {PractitionersMapper.class})
public interface CaseMapper {

    @Mapping(target = "dates", ignore = true) // mapped in AfterMapping
    @Mapping(target = "links", ignore = true) // mapped in AfterMapping
    @Mapping(target = "notes", ignore = true) // doesn't exist on source
    @Mapping(target = "practitioners", source = "appointments")
    @Mapping(target = "number", source = "caseNumber")
    @Mapping(target = "type", source = "caseType")
    @ValueMappings({
            @ValueMapping(target = "MEMBERS_VOLUNTARY_LIQUIDATION", source =
                    "MEMBERS_VOLUNTARY_LIQUIDATION"),
            @ValueMapping(target = "CREDITORS_VOLUNTARY_LIQUIDATION", source =
                    "CREDITORS_VOLUNTARY_LIQUIDATION"),
            @ValueMapping(target = "COMPULSORY_LIQUIDATION", source = "COMPULSORY_LIQUIDATION"),
            @ValueMapping(target = "RECEIVER_MANAGER", source = "RECEIVER_MANAGER"),
            @ValueMapping(target = "ADMINISTRATIVE_RECEIVER", source = "ADMINISTRATIVE_RECEIVER"),
            @ValueMapping(target = "ADMINISTRATION_ORDER", source = "ADMINISTRATION"),
            @ValueMapping(target = "CORPORATE_VOLUNTARY_ARRANGEMENT", source =
                    "CORPORATE_VOLUNTARY_ARRANGEMENT"),
            @ValueMapping(target = "CORPORATE_VOLUNTARY_ARRANGEMENT", source =
                    "CORPORATE_VOLUNTARY_ARRANGEMENT_"),
            @ValueMapping(target = "IN_ADMINISTRATION", source = "IN_ADMINISTRATION"),
            @ValueMapping(target = "CORPORATE_VOLUNTARY_ARRANGEMENT_MORATORIUM", source =
                    "CVA_MORATORIA"),
            @ValueMapping(target = "FOREIGN_INSOLVENCY", source = "FOREIGN_INSOLVENCY"),
            @ValueMapping(target = "MORATORIUM", source = "MORATORIUM")})
    ModelCase map(CaseNumber sourceCase, @Context String companyNumber);

    /**
     * Invoked at the end of the auto-generated mapping methods and maps/sets the following:
     * - the case dates from the source (in the caseNumber object) to the CaseDates target object
     * - constructs the target Links object using the company number and mortgage_id from the
     * source.
     *
     * @param modelCase     the target ModelCase object
     * @param sourceCase    the source CaseNumber object
     * @param companyNumber the company number as a context object passed from the top level
     *                      source case
     */
    @AfterMapping
    default void mapDatesAndLinks(@MappingTarget ModelCase modelCase,
                                  CaseNumber sourceCase, @Context String companyNumber) {
        List<CaseDates> mappedCaseDates = MapperUtils.mapAndAggregateCaseDates(sourceCase);
        modelCase.setDates(mappedCaseDates);

        Optional<Long> optionalMortgageId = Optional.ofNullable(sourceCase.getMortgageId());
        optionalMortgageId.ifPresent(mortgageId -> {
            try {
                // Concatenate mortgageId with salt, hash using SHA-1 and then encode using base64
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                var salt = "jkdhjdio";
                String saltedMortgageId = mortgageId + salt;
                byte[] hash = digest.digest(saltedMortgageId.getBytes(StandardCharsets.UTF_8));
                // We remove the padding equals '=' appended at the end by the base64 encoding
                // (could be at most 2 = based on the length of the mortgage id).
                var encodedMortgageId = Base64Utils.encodeToUrlSafeString(hash).replace("=", "");

                var chargeLink = String.format(
                        "/company/%s/charges/%s", companyNumber, encodedMortgageId);

                Links links = new Links();
                links.setCharge(chargeLink);
                modelCase.setLinks(links);
            } catch (NoSuchAlgorithmException algorithmException) {
                throw new RuntimeException("SHA-1 was not available to hash mortgageId");
            }
        });
    }

}
