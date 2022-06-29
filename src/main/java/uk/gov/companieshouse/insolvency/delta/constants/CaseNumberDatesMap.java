package uk.gov.companieshouse.insolvency.delta.constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaseNumberDatesMap {
    private static final Map<Integer, List<String>> caseDates = new HashMap<>() {
        {
            List<String> allDatesList = Arrays.asList("adminEndDate", "adminOrderDate",
                    "adminStartDate", "appointmentDate", "completionDate",
                    "dischargeAdminOrderDate", "dissolvedDate", "dissolvedDueDate",
                    "instrumentDate", "petitionDate", "reportDate", "swornDate",
                    "windUpConclusionDate", "windUpDate", "endDate");
            put(0, allDatesList);
            put(1, Arrays.asList("dissolvedDueDate","dissolvedDate","swornDate","windUpDate"));
            put(2, Arrays.asList("dissolvedDueDate","dissolvedDate","windUpDate"));
            put(3, Arrays.asList("petitionDate","windUpDate","windUpConclusionDate",
                    "dissolvedDate","dissolvedDueDate"));
            put(5, Arrays.asList("instrumentDate"));
            put(6, Arrays.asList("instrumentDate"));
            put(7, Arrays.asList("adminOrderDate","dischargeAdminOrderDate"));
            put(8, Arrays.asList("reportDate","completionDate"));
            put(13, Arrays.asList("adminStartDate","adminEndDate"));
            put(14, Arrays.asList("appointmentDate","endDate"));
            put(17, Arrays.asList("appointmentDate","endDate"));
        }
    };

    public static Map<Integer, List<String>> getCaseDates() {
        return caseDates;
    }
}
