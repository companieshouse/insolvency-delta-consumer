package uk.gov.companieshouse.insolvency.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.ADMINISTRATION_ORDER;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.ADMINISTRATIVE_RECEIVER;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.IN_ADMINISTRATION;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.LIQUIDATION;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.LIVE_RECEIVER_MANAGER_ON_AT_LEAST_ONE_CHARGE;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.RECEIVERSHIP;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.RECEIVER_MANAGER;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.VOLUNTARY_ARRANGEMENT;
import static uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum.VOLUNTARY_ARRANGEMENT_RECEIVERSHIP;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum;

public class InsolvencyStatusMapperTest {

    private final InsolvencyStatusMapper insolvencyStatusMapper = new InsolvencyStatusMapper();

    @ParameterizedTest
    @MethodSource("statusEnumArgs")
    void shouldMapKeyToCorrectStatusEnum(final String key, List<StatusEnum> expected) {
        // given

        // when
        List<StatusEnum> actual = insolvencyStatusMapper.mapStatus(key);

        // then
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> statusEnumArgs() {
        return Stream.of(
                Arguments.of("2", List.of(LIQUIDATION)),
                Arguments.of("3", List.of(RECEIVERSHIP)),
                Arguments.of("A", List.of(RECEIVERSHIP)),
                Arguments.of("C", List.of(VOLUNTARY_ARRANGEMENT_RECEIVERSHIP)),
                Arguments.of("E", List.of(ADMINISTRATION_ORDER, RECEIVERSHIP)),
                Arguments.of("F", List.of(LIVE_RECEIVER_MANAGER_ON_AT_LEAST_ONE_CHARGE)),
                Arguments.of("G", List.of(ADMINISTRATIVE_RECEIVER)),
                Arguments.of("H", List.of(RECEIVER_MANAGER, ADMINISTRATIVE_RECEIVER)),
                Arguments.of("I", List.of(VOLUNTARY_ARRANGEMENT)),
                Arguments.of("J", List.of(VOLUNTARY_ARRANGEMENT, RECEIVER_MANAGER)),
                Arguments.of("K", List.of(VOLUNTARY_ARRANGEMENT, ADMINISTRATIVE_RECEIVER)),
                Arguments.of("L", List.of(VOLUNTARY_ARRANGEMENT, ADMINISTRATIVE_RECEIVER, RECEIVER_MANAGER)),
                Arguments.of("M", List.of(ADMINISTRATION_ORDER)),
                Arguments.of("N", List.of(ADMINISTRATION_ORDER, RECEIVER_MANAGER)),
                Arguments.of("O", List.of(ADMINISTRATION_ORDER, ADMINISTRATIVE_RECEIVER)),
                Arguments.of("P", List.of(ADMINISTRATION_ORDER, RECEIVER_MANAGER)),
                Arguments.of("S", List.of(IN_ADMINISTRATION, RECEIVERSHIP)),
                Arguments.of("T", List.of(IN_ADMINISTRATION)),
                Arguments.of("U", List.of(IN_ADMINISTRATION, RECEIVER_MANAGER)),
                Arguments.of("V", List.of(IN_ADMINISTRATION, ADMINISTRATIVE_RECEIVER)),
                Arguments.of("W", List.of(IN_ADMINISTRATION, RECEIVER_MANAGER, ADMINISTRATIVE_RECEIVER)),

                // Should return null
                Arguments.of("1", null),
                Arguments.of("Z", null),
                Arguments.of(null, null)
        );
    }
}
