package com.solubris;

import org.apache.beam.io.requestresponse.RequestResponseIO;
import org.apache.beam.io.requestresponse.Result;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.ListCoder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.influxdb.InfluxDbIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.beam.sdk.transforms.MapElements.into;
import static org.apache.beam.sdk.values.TypeDescriptors.strings;

public class Scrape {
    public static void main(String[] args) {
        ScrapeOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(ScrapeOptions.class);

        run(options);
    }

    public interface ScrapeOptions extends PipelineOptions {
        @Validation.Required
        String getUrl();

        void setUrl(String value);
    }

    public static void run(ScrapeOptions options) {
        Pipeline pipeline = Pipeline.create(options);
        PCollection<KV<String, String>> input = pipeline.apply(Create.of(
                KV.of(options.getUrl(), null)
//                KV.of("http://example.com", "/html/body/div/h1")
//                KV.of("https://www.rhs.org.uk/shows-events/rhs-chelsea-flower-show/ticket-options", "/html/body/form/div[3]/div[2]/div/div/app-root/app-general-page/ngx-dynamic-hooks/rhs-widgets-content/div/article/div[1]/div[2]/div/div[1]/ul[4]/li[2]/div[2]")
//                KV.of("https://www.rhs.org.uk/shows-events/rhs-chelsea-flower-show/ticket-options", "html.show.t-chelsea.flexbox.flexboxlegacy.csscolumns.csscolumns-width.csscolumns-span.csscolumns-fill.csscolumns-gap.csscolumns-rule.csscolumns-rulecolor.csscolumns-rulestyle.csscolumns-rulewidth.no-csscolumns-breakbefore.no-csscolumns-breakafter.no-csscolumns-breakinside body.LTR.Gecko.Gecko134.ENGB.ContentBody.f2 form#aspnetForm div#content.is-menu-scrolled div#ctl00_BaseClass div#skip-content div.container.clr app-root app-general-page.ng-star-inserted ngx-dynamic-hooks.ng-star-inserted rhs-widgets-content div.show-ticket-wrapper article.show-tickets div.ticket-details div.ticketing-column div.ticketing-card div ul.border-btm.margin-btm li.item div.item-value")
//                KV.of("https://www.rhs.org.uk/shows-events/rhs-chelsea-flower-show/ticket-options", ".item-value")
        ));

        Result<List<ShowPrice>> response = input.apply(RequestResponseIO.of(new UrlCaller(), ListCoder.of(SerializableCoder.of(ShowPrice.class))));
        response.getFailures().apply("logFailures", ParDo.of(new LogOutput<>("Failures: ")));

        String hostUrl = "https://us-central1-1.gcp.cloud2.influxdata.com";
        String authToken = "1ikPPy3JuVXMSwk3zvf5xcFd-Xof2bWXK1JS7rCilj8yzmyHFcDiyrk7a35XF8bLVZtSiXdW66_9vqMCH-_4BQ==";

        PCollection<List<ShowPrice>> result = response.getResponses();
        PCollection<ShowPrice> results =
                result.apply(
                        FlatMapElements.via(
                                new SimpleFunction<List<ShowPrice>, Iterable<ShowPrice>>() {
                                    public Iterable<ShowPrice> apply(List<ShowPrice> input) {
                                        return input;
                                    }
                                }
                        ));
        // write the doubles to a db - BigQuery?
        results.apply("logSucceeded", ParDo.of(new LogOutput<>("Output from calling: ")))
                .apply(Filter.by(s -> s.getValue() != null))
                .apply(into(strings()).via(price -> price.toInfluxLine()))
                .apply(InfluxDbIO.write()
                        .withDatabase("scraped")
                        .withDataSourceConfiguration(InfluxDbIO.DataSourceConfiguration.create(StaticValueProvider.of(hostUrl),
                                StaticValueProvider.of("lithium147@gmail.com"),
                                StaticValueProvider.of(authToken)))
                );

        pipeline.run();
//        pipeline.run().waitUntilFinish();
    }

    static class LogOutput<T> extends DoFn<T, T> {
        private static final Logger LOG = LoggerFactory.getLogger(LogOutput.class);
        private final String prefix;

        public LogOutput(String prefix) {
            this.prefix = prefix;
        }

        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            LOG.info(prefix + c.element());
            c.output(c.element());
        }
    }
}
