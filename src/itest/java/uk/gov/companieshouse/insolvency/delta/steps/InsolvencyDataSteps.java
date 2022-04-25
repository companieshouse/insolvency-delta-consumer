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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.util.ResourceUtils;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InsolvencyDataSteps {

    private static WireMockServer wireMockServer;

    private String companyNumber;

    @Value("${insolvency.delta.topic}")
    private String topic;

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    public KafkaConsumer<String, Object> kafkaConsumer;

    @Given("Insolvency delta consumer service is running")
    public void insolvency_delta_consumer_service_is_running() {
        ResponseEntity<String> response = restTemplate.getForEntity("/healthcheck", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(200));
        assertThat(response.getBody()).isEqualTo("I am healthy");
    }

    @When("a {string} with {string} is published to the topic {string} and consumed")
    public void a_with_is_published_to_the_topic_and_consumed(String message, String companyNumber, String topicName)
            throws InterruptedException {
        startWiremockServer();
        this.companyNumber = companyNumber;

        stubInsolvencyDataApiServiceCalls(companyNumber, 200);
        kafkaTemplate.send(topicName, createMessage(message));

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @When("a non-avro message is published and failed to process")
    public void a_non_avro_message_is_published_and_failed_to_process() throws InterruptedException {
        startWiremockServer();
        kafkaTemplate.send(topic, "invalid message");

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @When("a valid message is published with invalid json")
    public void a_valid_message_is_published_with_invalid_json() throws InterruptedException {
        startWiremockServer();
        String invalidJson = loadFile("classpath:input/case_type_invalid.json");
        ChsDelta chsDelta = ChsDelta.newBuilder()
                .setData(invalidJson)
                .setContextId("context_id")
                .setAttempt(1)
                .build();
        kafkaTemplate.send(topic, chsDelta);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @When("a message {string} is published for {string} and stubbed insolvency-data-api returns {string}")
    public void a_message_is_published_for_and_stubbed_insolvency_data_api_returns(
            String message, String companyNumber, String statusCode) throws InterruptedException {
        startWiremockServer();
        this.companyNumber = companyNumber;

        stubInsolvencyDataApiServiceCalls(companyNumber, Integer.parseInt(statusCode));
        kafkaTemplate.send(topic, createMessage(message));

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @When("a message {string} is published for {string} with unexpected data")
    public void a_message_is_published_for_with_unexpected_data(String message, String companyNumber) throws InterruptedException {
        startWiremockServer();
        this.companyNumber = companyNumber;
        kafkaTemplate.send(topic, createMessage(message));

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @Then("the message should be moved to topic {string}")
    public void the_message_should_be_moved_to_topic(String topic) {
        ConsumerRecord<String, Object> singleRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, topic);

        assertThat(singleRecord.value()).isNotNull();

        wireMockServer.stop();
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

    private void stubInsolvencyDataApiServiceCalls(String chNumber, int statusCode) {
        stubFor(
                put(urlPathEqualTo("/company/"+chNumber+"/insolvency"))
                        .withRequestBody(containing(chNumber))
                        .willReturn(aResponse()
                                .withStatus(statusCode)));
    }

    private void startWiremockServer() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
        configureFor("localhost", 8888);
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
