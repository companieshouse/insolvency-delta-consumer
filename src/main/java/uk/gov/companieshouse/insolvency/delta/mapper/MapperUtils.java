package uk.gov.companieshouse.insolvency.delta.mapper;

import static uk.gov.companieshouse.api.delta.CaseNumber.CaseTypeEnum.ADMINISTRATION;
import static uk.gov.companieshouse.api.delta.CaseNumber.CaseTypeEnum.FOREIGN_INSOLVENCY;
import static uk.gov.companieshouse.api.delta.CaseNumber.CaseTypeEnum.IN_ADMINISTRATION;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.ADMINISTRATION_DISCHARGED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.ADMINISTRATION_ENDED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.ADMINISTRATION_STARTED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.CONCLUDED_WINDING_UP_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.DECLARATION_SOLVENT_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.DISSOLVED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.DUE_TO_BE_DISSOLVED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.INSTRUMENTED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.MORATORIUM_ENDED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.MORATORIUM_STARTED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.PETITIONED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.VOLUNTARY_ARRANGEMENT_ENDED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.VOLUNTARY_ARRANGEMENT_STARTED_ON;
import static uk.gov.companieshouse.api.insolvency.CaseDates.TypeEnum.WOUND_UP_ON;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.insolvency.CaseDates;

public final class MapperUtils {

    // make the class noninstantiable
    private MapperUtils() {
    }

    /**
     * NB: Source does not have a corresponding date to map to the following target enum members:
     * CASE_END_ON, ORDERED_TO_WIND_UP_ON.
     *
     * @param sourceCase the source case containing all dates as non-nested properties
     * @return a map of target enum values and the corresponding source case dates
     */
    private static EnumMap<CaseDates.TypeEnum, String> createMapping(CaseNumber sourceCase) {
        EnumMap<CaseDates.TypeEnum, String> datesWithTargetEnums =
                new EnumMap<>(CaseDates.TypeEnum.class);

        datesWithTargetEnums.put(INSTRUMENTED_ON, sourceCase.getInstrumentDate());
        datesWithTargetEnums.put(ADMINISTRATION_DISCHARGED_ON,
                sourceCase.getDischargeAdminOrderDate());
        datesWithTargetEnums.put(ADMINISTRATION_ENDED_ON, sourceCase.getAdminEndDate());
        datesWithTargetEnums.put(CONCLUDED_WINDING_UP_ON, sourceCase.getWindUpConclusionDate());
        datesWithTargetEnums.put(PETITIONED_ON, sourceCase.getPetitionDate());
        datesWithTargetEnums.put(DUE_TO_BE_DISSOLVED_ON, sourceCase.getDissolvedDueDate());
        datesWithTargetEnums.put(WOUND_UP_ON, sourceCase.getWindUpDate());
        datesWithTargetEnums.put(VOLUNTARY_ARRANGEMENT_STARTED_ON, sourceCase.getReportDate());
        datesWithTargetEnums.put(VOLUNTARY_ARRANGEMENT_ENDED_ON, sourceCase.getCompletionDate());
        datesWithTargetEnums.put(MORATORIUM_STARTED_ON, sourceCase.getAppointmentDate());
        datesWithTargetEnums.put(DECLARATION_SOLVENT_ON, sourceCase.getSwornDate());
        datesWithTargetEnums.put(DISSOLVED_ON, sourceCase.getDissolvedDate());
        datesWithTargetEnums.put(MORATORIUM_ENDED_ON, sourceCase.getEndDate());

        // Special mapping cases
        if (sourceCase.getCaseType() == FOREIGN_INSOLVENCY) {
            datesWithTargetEnums.replace(MORATORIUM_STARTED_ON, null);
        }
        if (sourceCase.getCaseType() == ADMINISTRATION) {
            datesWithTargetEnums.put(ADMINISTRATION_STARTED_ON, sourceCase.getAdminOrderDate());
        }
        if (sourceCase.getCaseType() == IN_ADMINISTRATION) {
            datesWithTargetEnums.put(ADMINISTRATION_STARTED_ON, sourceCase.getAdminStartDate());
        }

        return datesWithTargetEnums;
    }

    private static CaseDates createCaseDate(String sourceDate, CaseDates.TypeEnum dateType) {
        CaseDates caseDate = new CaseDates();
        caseDate.setType(dateType);
        caseDate.setDate(LocalDate.parse(sourceDate, DateTimeFormatter.ofPattern("yyyyMMdd")));
        return caseDate;
    }

    /**
     * Converts the dates in the source case to the target model comprised of an Enum matching the
     * name of the date property and the LocalDate parsed from its string representation in the
     * source. Returns a list of CaseDates objects to be used in the target ModelCase.
     *
     * @param sourceCase the source case containing all dates as non-nested properties
     * @return list of all non-empty CaseDates mapped from the source.
     */
    public static List<CaseDates> mapAndAggregateCaseDates(CaseNumber sourceCase) {
        Map<CaseDates.TypeEnum, String> extractedDatesFromSource = createMapping(sourceCase);

        return extractedDatesFromSource.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> createCaseDate(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }
}
