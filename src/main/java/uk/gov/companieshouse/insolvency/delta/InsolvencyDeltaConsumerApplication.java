package uk.gov.companieshouse.insolvency.delta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InsolvencyDeltaConsumerApplication {

    public static final String NAMESPACE = "insolvency-delta-consumer";

    public static void main(String[] args) {
        SpringApplication.run(InsolvencyDeltaConsumerApplication.class, args);
    }
}
