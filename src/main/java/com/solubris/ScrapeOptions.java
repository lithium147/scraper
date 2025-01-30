package com.solubris;

import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation;

public interface ScrapeOptions extends PipelineOptions {
    @Validation.Required
    String getUrl();

    void setUrl(String value);
}
