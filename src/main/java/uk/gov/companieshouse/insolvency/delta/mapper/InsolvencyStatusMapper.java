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
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency.StatusEnum;

@Component
public class InsolvencyStatusMapper {

    /**
     * @param key the chips key for insolvency status.
     * @return list of insolvency statuses.
     */
    public List<StatusEnum> mapStatus(final String key) {
        List<StatusEnum> status = null;

        if (StringUtils.isNotBlank(key)) {
            switch (key) {
                case "2":
                    status = List.of(LIQUIDATION);
                    break;
                case "3":
                case "A":
                    status = List.of(RECEIVERSHIP);
                    break;
                case "C":
                    status = List.of(VOLUNTARY_ARRANGEMENT_RECEIVERSHIP);
                    break;
                case "E":
                    status = List.of(ADMINISTRATION_ORDER, RECEIVERSHIP);
                    break;
                case "F":
                    status = List.of(LIVE_RECEIVER_MANAGER_ON_AT_LEAST_ONE_CHARGE);
                    break;
                case "G":
                    status = List.of(ADMINISTRATIVE_RECEIVER);
                    break;
                case "H":
                    status = List.of(RECEIVER_MANAGER, ADMINISTRATIVE_RECEIVER);
                    break;
                case "I":
                    status = List.of(VOLUNTARY_ARRANGEMENT);
                    break;
                case "J":
                    status = List.of(VOLUNTARY_ARRANGEMENT, RECEIVER_MANAGER);
                    break;
                case "K":
                    status = List.of(VOLUNTARY_ARRANGEMENT, ADMINISTRATIVE_RECEIVER);
                    break;
                case "L":
                    status = List.of(VOLUNTARY_ARRANGEMENT,
                            ADMINISTRATIVE_RECEIVER,
                            RECEIVER_MANAGER);
                    break;
                case "M":
                    status = List.of(ADMINISTRATION_ORDER);
                    break;
                case "N":
                case "P":
                    status = List.of(ADMINISTRATION_ORDER, RECEIVER_MANAGER);
                    break;
                case "O":
                    status = List.of(ADMINISTRATION_ORDER, ADMINISTRATIVE_RECEIVER);
                    break;
                case "S":
                    status = List.of(IN_ADMINISTRATION, RECEIVERSHIP);
                    break;
                case "T":
                    status = List.of(IN_ADMINISTRATION);
                    break;
                case "U":
                    status = List.of(IN_ADMINISTRATION, RECEIVER_MANAGER);
                    break;
                case "V":
                    status = List.of(IN_ADMINISTRATION, ADMINISTRATIVE_RECEIVER);
                    break;
                case "W":
                    status = List.of(IN_ADMINISTRATION, RECEIVER_MANAGER, ADMINISTRATIVE_RECEIVER);
                    break;
                default:
                    break;
            }
        }
        return status;
    }
}
