package uk.gov.companieshouse.insolvency.delta.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.companieshouse.insolvency.delta.mapper.EncoderUtil;

@TestConfiguration
public class TestConfig {

    @Bean
    public EncoderUtil encoderUtil() {
        return new EncoderUtil("testsalt");
    }
}
