package com.vladislavbalyuk.tracker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {
    @JsonCreator
    public Result(@JsonProperty("formatted_address") String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    @JsonProperty("formatted_address")
    private String formattedAddress;

    @JsonProperty("formatted_address")
    public String getFormattedAddress() {
        return formattedAddress;
    }

    @JsonProperty("formatted_address")
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}
