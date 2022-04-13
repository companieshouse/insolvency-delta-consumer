package uk.gov.companieshouse.insolvency.delta.steps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.ResourceUtils;
import uk.gov.companieshouse.delta.ChsDelta;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;

public class InsolvencyDataSteps {

    private static WireMockServer wireMockServer;

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Given("Insolvency delta consumer service is running")
    public void insolvency_delta_consumer_service_is_running() {
        ResponseEntity<String> response = restTemplate.getForEntity("/healthcheck", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(200));
        assertThat(response.getBody()).isEqualTo("I am healthy");

        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
        configureFor("localhost", 8888);

        stubInsolvencyDataApiServiceCalls();
    }

    @When("a message is published to the topic {string} and consumed")
    public void a_message_is_published_to_the_topic_and_consumed(String topicName) throws InterruptedException {
        kafkaTemplate.send(topicName, createMessage());

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @Then("verify PUT method is called on insolvency-data-api service with response status code as 200")
    public void the_insolvency_delta_consumer_should_consume_and_process_the_message() {
        verify(1, putRequestedFor(urlPathEqualTo("/company/01876743/insolvency")));

        wireMockServer.stop();
    }

    @Then("verify PUT method is called on insolvency-data-api service")
    public void verify_put_method_with_request_is_called_on_insolvency_data_api_service() {
        verify(1, putRequestedFor(urlEqualTo("/company/01876743/insolvency"))
                .withRequestBody(matchingJsonPath("$.external_data"))
                .withRequestBody(matchingJsonPath("$.internal_data")));

        wireMockServer.stop();
    }

    private void stubInsolvencyDataApiServiceCalls() {
        stubFor(
                put(urlPathEqualTo("/company/01876743/insolvency"))
                        .withRequestBody(containing("01876743"))
                        .willReturn(aResponse()
                                .withStatus(200)));
    }

    private String loadFile(String fileName) {
        try {
            return FileUtils.readFileToString(ResourceUtils.getFile(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to locate file %s", fileName));
        }
    }

    private ChsDelta createMessage() {
        String insolvencyData = loadFile("classpath:data/insolvency-delta-complex.json");
        return ChsDelta.newBuilder()
                .setData(insolvencyData)
                .setContextId("context_id")
                .setAttempt(1)
                .build();
    }
}
