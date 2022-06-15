package uk.gov.companieshouse.insolvency.delta.steps;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import uk.gov.companieshouse.delta.ChsDelta;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static uk.gov.companieshouse.insolvency.delta.config.WiremockTestConfig.createDeleteMessage;
import static uk.gov.companieshouse.insolvency.delta.config.WiremockTestConfig.createMessage;
import static uk.gov.companieshouse.insolvency.delta.config.WiremockTestConfig.loadFile;
import static uk.gov.companieshouse.insolvency.delta.config.WiremockTestConfig.setupWiremock;
import static uk.gov.companieshouse.insolvency.delta.config.WiremockTestConfig.stop;
import static uk.gov.companieshouse.insolvency.delta.config.WiremockTestConfig.stubInsolvencyDataApiServiceCalls;
import static uk.gov.companieshouse.insolvency.delta.config.WiremockTestConfig.stubInsolvencyDeleteDataApiServiceCalls;

public class InsolvencyDataSteps {

    private static WireMockServer wireMockServer;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private String companyNumber;

    @Value("${insolvency.delta.topic}")
    private String topic;

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    public KafkaConsumer<String, Object> kafkaConsumer;

    @Before
    public static void before_each() {
        setupWiremock();
    }

    @After
    public static void after_each() {
        stop();
    }

    @Given("Insolvency delta consumer service is running")
    public void insolvency_delta_consumer_service_is_running() {
        ResponseEntity<String> response = restTemplate.getForEntity("/insolvency-delta-consumer/healthcheck", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(200));
        assertThat(response.getBody()).isEqualTo("{\"status\":\"UP\"}");
    }

    @When("a {string} with {string} is published to the topic {string} and consumed")
    public void a_with_is_published_to_the_topic_and_consumed(String message, String companyNumber, String topicName)
            throws InterruptedException {
        this.companyNumber = companyNumber;

        stubInsolvencyDataApiServiceCalls(companyNumber, 200);
        sendMessage(topicName, createMessage(message));

        assertFalse(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @When("a {string} delete event is published to the topic {string} with insolvency data endpoint returning {string}")
    public void a_delete_event_is_published_to_the_topic_with_insolvency_data_endpoint_returning(
            String message, String topic, String statusCode) throws InterruptedException {
        stubInsolvencyDeleteDataApiServiceCalls(this.companyNumber, Integer.parseInt(statusCode));
        sendMessage(topic, createDeleteMessage(message));

        assertFalse(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @When("a delete event message {string} is published to the topic {string}")
    public void a_delete_event_message_is_published_to_the_topic(String message, String topic) throws InterruptedException {
        stubInsolvencyDeleteDataApiServiceCalls(this.companyNumber, 200);
        sendMessage(topic, createDeleteMessage(message));

        assertFalse(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @When("a non-avro {string} is published and failed to process")
    public void a_non_avro_is_published_and_failed_to_process(String message) throws InterruptedException {
        kafkaTemplate.send(topic, message);

        assertFalse(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @When("a valid message is published with invalid json")
    public void a_valid_message_is_published_with_invalid_json() throws InterruptedException {
        String invalidJson = loadFile("classpath:input/case_type_invalid.json");
        ChsDelta chsDelta = ChsDelta.newBuilder()
                .setData(invalidJson)
                .setContextId("context_id")
                .setAttempt(1)
                .build();
        sendMessage(topic, chsDelta);

        assertFalse(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @When("a message {string} is published for {string} and stubbed insolvency-data-api returns {string}")
    public void a_message_is_published_for_and_stubbed_insolvency_data_api_returns(
            String message, String companyNumber, String statusCode) throws InterruptedException {
        this.companyNumber = companyNumber;

        stubInsolvencyDataApiServiceCalls(companyNumber, Integer.parseInt(statusCode));
        sendMessage(topic, createMessage(message));

        assertFalse(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @When("a message {string} is published for {string} with unexpected data")
    public void a_message_is_published_for_with_unexpected_data(String message, String companyNumber) throws InterruptedException {
        this.companyNumber = companyNumber;
        sendMessage(topic, createMessage(message));

        assertFalse(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @Then("verify the insolvency data endpoint is invoked successfully")
    public void verify_the_insolvency_data_endpoint_is_invoked_successfully() {
        verify(1, deleteRequestedFor(urlMatching("/company/"+this.companyNumber+"/insolvency")));
    }

    @Then("the message should be moved to topic {string}")
    public void the_message_should_be_moved_to_topic(String topic) {
        ConsumerRecord<String, Object> singleRecord =
                KafkaTestUtils.getSingleRecord(kafkaConsumer, topic, 2000L);

        assertThat(singleRecord.value()).isNotNull();
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
    }

    private void sendMessage(String topic, ChsDelta message) {
        kafkaTemplate.send(topic, message);
        kafkaTemplate.flush();
    }
}
