package uk.gov.companieshouse.insolvency.delta.mapper;

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
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum;

@Component
public class InsolvencyStatusMapper {

    private static final Map<String, List<StatusEnum>> STATUS_MAP = Map.ofEntries(
            Map.entry("2", List.of(LIQUIDATION)),
            Map.entry("3", List.of(RECEIVERSHIP)),
            Map.entry("A", List.of(RECEIVERSHIP)),
            Map.entry("C", List.of(VOLUNTARY_ARRANGEMENT_RECEIVERSHIP)),
            Map.entry("E", List.of(ADMINISTRATION_ORDER, RECEIVERSHIP)),
            Map.entry("F", List.of(LIVE_RECEIVER_MANAGER_ON_AT_LEAST_ONE_CHARGE)),
            Map.entry("G", List.of(ADMINISTRATIVE_RECEIVER)),
            Map.entry("H", List.of(RECEIVER_MANAGER, ADMINISTRATIVE_RECEIVER)),
            Map.entry("I", List.of(VOLUNTARY_ARRANGEMENT)),
            Map.entry("J", List.of(VOLUNTARY_ARRANGEMENT, RECEIVER_MANAGER)),
            Map.entry("K", List.of(VOLUNTARY_ARRANGEMENT, ADMINISTRATIVE_RECEIVER)),
            Map.entry("L",
                    List.of(VOLUNTARY_ARRANGEMENT,
                    ADMINISTRATIVE_RECEIVER,
                    RECEIVER_MANAGER)),
            Map.entry("M", List.of(ADMINISTRATION_ORDER)),
            Map.entry("N", List.of(ADMINISTRATION_ORDER, RECEIVER_MANAGER)),
            Map.entry("O", List.of(ADMINISTRATION_ORDER, ADMINISTRATIVE_RECEIVER)),
            Map.entry("P", List.of(ADMINISTRATION_ORDER, RECEIVER_MANAGER)),
            Map.entry("S", List.of(IN_ADMINISTRATION, RECEIVERSHIP)),
            Map.entry("T", List.of(IN_ADMINISTRATION)),
            Map.entry("U", List.of(IN_ADMINISTRATION, RECEIVER_MANAGER)),
            Map.entry("V", List.of(IN_ADMINISTRATION, ADMINISTRATIVE_RECEIVER)),
            Map.entry("W", List.of(IN_ADMINISTRATION, RECEIVER_MANAGER, ADMINISTRATIVE_RECEIVER))
    );

    /**
     * @param key the chips key for insolvency status.
     * @return list of insolvency statuses.
     */
    public List<StatusEnum> mapStatus(final String key) {
        return StringUtils.isNotBlank(key) ? STATUS_MAP.getOrDefault(key, null) : null;
    }
}
