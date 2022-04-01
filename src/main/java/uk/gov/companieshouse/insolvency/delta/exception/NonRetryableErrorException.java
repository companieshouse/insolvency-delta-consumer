package uk.gov.companieshouse.insolvency.delta.exception;

public class NonRetryableErrorException extends RuntimeException {
    public NonRetryableErrorException(String message) {
        super(message);
    }

    public NonRetryableErrorException(Exception exception) {
        super(exception);
    }
}

