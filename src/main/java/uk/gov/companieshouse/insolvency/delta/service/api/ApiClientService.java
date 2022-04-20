
package uk.gov.companieshouse.insolvency.delta.service.api;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.model.ApiResponse;

/**
 * The {@code ApiClientService} interface provides an abstraction that can be
 * used when testing {@code ApiClientManager} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface ApiClientService {

    InternalApiClient getApiClient(String contextId);

    /**
     * Submit insolvency.
     */
    ApiResponse<Void> putInsolvency(
            final String log,
            final String companyNumber,
            final InternalCompanyInsolvency internalCompanyInsolvency);

    /**
     * Delete insolvency.
     */
    ApiResponse<Void> deleteInsolvency(
            final String log,
            final String companyNumber);
}
