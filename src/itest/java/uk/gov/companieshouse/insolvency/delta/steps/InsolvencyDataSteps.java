package uk.gov.companieshouse.insolvency.delta.steps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
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
import static org.assertj.core.api.Assertions.assertThat;

public class InsolvencyDataSteps {

    private static WireMockServer wireMockServer;

    private String companyNumber;

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Given("Insolvency delta consumer service is running")
    public void insolvency_delta_consumer_service_is_running() {
        ResponseEntity<String> response = restTemplate.getForEntity("/healthcheck", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(200));
        assertThat(response.getBody()).isEqualTo("I am healthy");
    }

    @When("a {string} with {string} is published to the topic {string} and consumed")
    public void a_with_is_published_to_the_topic_and_consumed(String message, String companyNumber, String topicName)
            throws InterruptedException {
        this.companyNumber = companyNumber;

        stubInsolvencyDataApiServiceCalls(companyNumber);
        kafkaTemplate.send(topicName, createMessage(message));

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @Then("verify PUT method is called on insolvency-data-api service with body {string}")
    public void verify_put_method_is_called_on_insolvency_data_api_service_with_body(String output) {
        String expectedBody = loadFile("classpath:output/"+output+".json");
        verify(1, putRequestedFor(urlMatching("/company/"+this.companyNumber+"/insolvency")));

        List<ServeEvent> allServeEvents = getAllServeEvents();
        ServeEvent serveEvent = allServeEvents.get(0);
        String actualBody = serveEvent.getRequest().getBodyAsString();

        assertThat(serveEvent.getResponse().getStatus()).isEqualTo(200);
        assertThat(actualBody).isEqualTo(expectedBody);

        wireMockServer.stop();
    }

    private void stubInsolvencyDataApiServiceCalls(String chNumber) {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
        configureFor("localhost", 8888);
        stubFor(
                put(urlPathEqualTo("/company/"+chNumber+"/insolvency"))
                        .withRequestBody(containing(chNumber))
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

    private ChsDelta createMessage(String message) {
        String insolvencyData = loadFile("classpath:input/"+message+".json");
        return ChsDelta.newBuilder()
                .setData(insolvencyData)
                .setContextId("context_id")
                .setAttempt(1)
                .build();
    }
}
