package uk.gov.companieshouse.insolvency.delta.validation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.CaseNumber;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.insolvency.delta.constants.CaseNumberDatesMap;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;

@Component
public class InsolvencyDeltaValidator {

    /**
     * Validate case dates for an insolvency.
     */
    public void validateCaseDates(Insolvency insolvency) throws Exception {

        List<CaseNumber> caseNumbers = insolvency.getCaseNumbers();
        Field[] fields = CaseNumber.class.getDeclaredFields();
        Map<Integer, List<String>> caseDates = CaseNumberDatesMap.getCaseDates();

        for (CaseNumber caseNumber:caseNumbers) {
            List<String> listOfFieldsToValidate =
                    buildListOfFieldsToValidateForSchema(caseNumber, caseDates);
            if (checkFieldAgainstSchema(fields, caseNumber, listOfFieldsToValidate)) {
                throw new NonRetryableErrorException("Unexpected fields present "
                        + "for case type Id " + caseNumber.getCaseTypeId());
            }
        }
    }

    private boolean checkFieldAgainstSchema(Field[] fields, CaseNumber caseNumber,
                                            List<String> listOfFieldsToValidate) throws Exception {
        for (Field field: fields) {
            String formattedFieldName = field.getName().substring(0, 1).toUpperCase()
                    + field.getName().substring(1);
            Method getter = CaseNumber.class.getMethod("get" + formattedFieldName);
            if (listOfFieldsToValidate.contains(field.getName())
                    & getter.invoke(caseNumber) != null) {
                return true;
            }
        }
        return false;
    }

    private List<String> buildListOfFieldsToValidateForSchema(
            CaseNumber caseNumber, Map<Integer, List<String>> caseDates) {
        return caseDates.get(0).stream().filter(
                Predicate.not(caseDates.get(caseNumber.getCaseTypeId().getValue())::contains))
                .collect(Collectors.toList());
    }
}