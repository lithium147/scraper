package com.solubris;

import org.apache.beam.io.requestresponse.Caller;
import org.apache.beam.io.requestresponse.UserCodeExecutionException;
import org.apache.beam.sdk.values.KV;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UrlCaller implements Caller<KV<String, String>, List<ShowPrice>> {
    private static final Logger LOG = LoggerFactory.getLogger(UrlCaller.class);

    public List<ShowPrice> call(KV<String, String> request) throws UserCodeExecutionException {
        List<ShowPrice> result = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(request.getKey()).get();

            for (Element parents : doc.select(".ticketing-column")) {
                String type = parents.select("h2").text();

                for (Element element : parents.select(".date")) {
                    Element dateSection = element.parent();
                    String date = dateSection.select(".date").text();

                    for (Element time : dateSection.select(".item")) {
                        String duration = time.selectFirst("div").text();
                        String value = time.select(".item-value").text();
                        result.add(new ShowPrice(type, date, duration, this.parseDoubleSafely(value)));
                    }
                }
            }

            return result;
        } catch (IOException e) {
            throw new UserCodeExecutionException(e);
        }
    }

    private Double parseDoubleSafely(String text) {
        try {
            text = text.replace("£", "");
            text = text.replace("$", "");
            text = text.replace("€", "");
            return Double.parseDouble(text);
        } catch (NumberFormatException var3) {
            LOG.warn("couldn't parse '{}'", text);
            return null;
        }
    }
}
