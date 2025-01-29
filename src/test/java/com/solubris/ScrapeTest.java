package com.solubris;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.ibm.icu.impl.locale.XCldrStub.FileUtilities.UTF8;

@RunWith(JUnit4.class)
public class ScrapeTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    private String getFilePath(String filePath) {
        if (filePath.contains(":")) {
            return filePath.replace("\\", "/").split(":", -1)[1];
        }
        return filePath;
    }

    /**
     * Want to test using a stub html page, so don't hammer the real url's.
     * Either mock the http client or use something like wire mock.
     */
    @Test
    public void canScrape() throws Exception {
        stubFor(get("/")
//                .withHeader("Content-Type", containing("xml"))
                .willReturn(ok()
                        .withHeader("Content-Type", "text/html; charset=utf-8")
                        .withBody(IOUtils.resourceToString("/scrape.html", UTF8))));

        int port = wireMockRule.port();
        Scrape.ScrapeOptions options = TestPipeline.testingPipelineOptions().as(Scrape.ScrapeOptions.class);
        options.setUrl("http://localhost:" + port + "/");
        // need the price name also, so can work out which price is which
        // seems like need to model all the prices on the page
        // type: member/public
        // day: tue/wed/thur/fri/sat
        // duration: all-day/half-day
        Scrape.run(options);
    }
}
