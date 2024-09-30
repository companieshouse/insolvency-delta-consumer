package uk.gov.companieshouse.insolvency.delta.validation;

import static uk.gov.companieshouse.insolvency.delta.InsolvencyDeltaConsumerApplication.NAMESPACE;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class InsolvencyDeltaValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    /**
     * Validate case dates for an insolvency.
     */
    public void validateCaseDates(Insolvency insolvency)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        List<CaseNumber> caseNumbers = insolvency.getCaseNumbers();
        Field[] fields = CaseNumber.class.getDeclaredFields();
        Map<Integer, List<String>> caseDates = CaseNumberDatesMap.getCaseDates();
        Map<Integer, String> mandatoryCaseTypesField =
                CaseNumberDatesMap.getMandatoryFieldsForCaseType();

        for (CaseNumber caseNumber : caseNumbers) {
            List<String> listOfFieldsToValidate =
                    buildListOfFieldsToValidateForSchema(caseNumber, caseDates);
            if (checkFieldAgainstSchema(fields, caseNumber, listOfFieldsToValidate)) {
                final String msg = String.format("Unexpected fields present for case type ID: %s",
                        caseNumber.getCaseTypeId());
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new NonRetryableErrorException(msg);
            }
            checkForMandatoryCaseDate(caseNumber, mandatoryCaseTypesField);
        }
    }

    /**
     * This method cycles through the fields array formatting each one to get the getter method for that field. The
     * field name is then checked against our list of fields to validate and the value of the getter is checked against
     * null
     */
    private boolean checkFieldAgainstSchema(Field[] fields, CaseNumber caseNumber,
            List<String> listOfFieldsToValidate)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        for (Field field : fields) {
            if (field.isSynthetic()) {
                continue;
            }

            if (listOfFieldsToValidate.contains(field.getName())
                    && checkIfFieldIsPresent(field.getName(), caseNumber)) {

                return true;
            }
        }
        return false;
    }

    /**
     * This method retrieves the zeroth entry of the caseDates map which contains all date fields. It then retrieves the
     * entry for our case number type Id and returns the all date fields entry with the dates from the case number entry
     * removed
     */
    private List<String> buildListOfFieldsToValidateForSchema(
            CaseNumber caseNumber, Map<Integer, List<String>> caseDates) {
        return caseDates.get(0).stream().filter(
                        Predicate.not(caseDates.get(caseNumber.getCaseTypeId().getValue())::contains))
                .collect(Collectors.toList());
    }

    private void checkForMandatoryCaseDate(CaseNumber caseNumber,
            Map<Integer, String> mandatoryCaseTypesField)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        int caseTypeId = caseNumber.getCaseTypeId().getValue();

        if (mandatoryCaseTypesField.containsKey(caseTypeId)
                && !checkIfFieldIsPresent(mandatoryCaseTypesField.get(caseTypeId), caseNumber)) {
            final String msg = String.format("Missing field: %s for case type ID: %d",
                    mandatoryCaseTypesField.get(caseTypeId),
                    caseTypeId);
            LOGGER.error(msg, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(msg);
        }
    }

    private boolean checkIfFieldIsPresent(String field, CaseNumber caseNumber)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String formattedFieldName =
                field.substring(0, 1).toUpperCase() + field.substring(1);
        Method getter = CaseNumber.class.getMethod("get" + formattedFieldName);

        return getter.invoke(caseNumber) != null;
    }
}