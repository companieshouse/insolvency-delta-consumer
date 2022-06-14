package uk.gov.companieshouse.insolvency.delta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Configuration class for logging.
 */
@Configuration
public class LoggingConfig {

    private static Logger staticLogger;

    @Value("${logger.namespace}")
    private String loggerNamespace;

    /**
     * Main application logger with component specific namespace.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        staticLogger = LoggerFactory.getLogger(loggerNamespace);
        return staticLogger;
    }

    public static Logger getLogger() {
        return staticLogger;
    }
}
