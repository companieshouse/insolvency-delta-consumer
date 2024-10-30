package uk.gov.companieshouse.insolvency.delta.apiclient;

import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

public interface ApiClient {

    void putInsolvency(String companyNumber, InternalCompanyInsolvency requestBody);
    void deleteInsolvency(String companyNumber, String deltaAt);
}
