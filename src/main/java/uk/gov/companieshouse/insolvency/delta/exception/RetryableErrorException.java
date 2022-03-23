package uk.gov.companieshouse.insolvency.delta.exception;

public class RetryableErrorException extends RuntimeException {
    public RetryableErrorException(String message) {
        super(message);
    }

    public RetryableErrorException(Exception exception) {
        super(exception);
    }
}

