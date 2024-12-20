package uk.gov.companieshouse.insolvency.delta.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;
import uk.gov.companieshouse.delta.ChsDelta;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WiremockTestConfig {

    private static int port = 8888;

    private static WireMockServer wireMockServer;

    public static void setupWiremock() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(port);
            start();
            configureFor("localhost", port);
        } else {
            restart();
        }
    }

    public static void start() {
        wireMockServer.start();
    }

    public static void stop() {
        wireMockServer.resetAll();
        wireMockServer.stop();
    }

    public static void restart() {
        stop();
        start();
    }

    public static void stubInsolvencyDataApiServiceCalls(String chNumber, int statusCode) {
        configureFor(port);
        stubFor(
                put(urlPathEqualTo("/company/"+chNumber+"/insolvency"))
                        .withRequestBody(containing(chNumber))
                        .willReturn(aResponse()
                                .withStatus(statusCode)));
    }

    public static void stubInsolvencyDeleteDataApiServiceCalls(String chNumber, int statusCode) {
        configureFor(port);
        stubFor(
                delete(urlPathEqualTo("/company/"+chNumber+"/insolvency"))
                        .willReturn(aResponse()
                                .withStatus(statusCode)));
    }

    public static String loadFile(String fileName) {
        try {
            return FileUtils.readFileToString(ResourceUtils.getFile(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to locate file %s", fileName));
        }
    }

    public static ChsDelta createMessage(String message) {
        String insolvencyData = loadFile("classpath:input/"+message+".json");
        return ChsDelta.newBuilder()
                .setData(insolvencyData)
                .setContextId("context_id")
                .setAttempt(1)
                .build();
    }

    public static ChsDelta createDeleteMessage(String message) {
        String insolvencyData = loadFile("classpath:input/"+message+".json");
        return ChsDelta.newBuilder()
                .setIsDelete(true)
                .setData(insolvencyData)
                .setContextId("context_id")
                .setAttempt(1)
                .build();
    }

}
