package com.solubris;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@RequiredArgsConstructor
@Getter
@ToString
public class ShowPrice implements Serializable {
    private final String type;
    private final String day;
    private final String time;
    private final Double value;

    public String toInfluxLine() {
        String tags = String.join(",", "RHS", keyValue("type", type), keyValue("day", day), keyValue("duration", time));
        return String.join(" ", tags, keyValue("price", Double.toString(value)));
    }

    private String keyValue(String key, String value) {
        value = value.replace(" ", "-");
        value = value.replace("’", "");
        return key + "=" + value;
    }
}
