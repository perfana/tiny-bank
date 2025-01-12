package io.perfana.tinybank.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class AccountWireMock {
    public static void main(String[] args) {

        WireMockConfiguration options = WireMockConfiguration.options()
                .port(30123)
                .disableRequestJournal()
                .asynchronousResponseEnabled(true)
                .asynchronousResponseThreads(256);

        WireMockServer wireMockServer = new WireMockServer(options);
        wireMockServer.start();

        WireMock.configureFor("localhost", 30123);

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/account?userId=u1234"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"customer\": { \"name\":  \"John Doe\" },  \"accountNumber\": \"LT121000011234567890\", \"name\": \"John's Tiny Payments Account\" }")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/account?userId=u5678"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"customer\": { \"name\":  \"Mary Jane\" },  \"accountNumber\": \"NL91ABNA0417164300\", \"name\": \"Mary's Tiny Savings Account\" }")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/account?userId=u9012"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"customer\": { \"name\":  \"Alice Coop\" },  \"accountNumber\": \"US12BOFA0000123456\", \"name\": \"Alice's Tiny Payments Account\" }")));

        System.out.println("WireMock server started at http://localhost:30123");
    }
}
