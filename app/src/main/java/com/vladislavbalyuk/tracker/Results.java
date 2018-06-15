package com.vladislavbalyuk.tracker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Results {
    @JsonCreator
    public Results(@JsonProperty("results") List<Result> results) {
        this.results = results;
    }

    @JsonProperty("results")
    private List<Result> results;

    @JsonProperty("results")
    public List<Result> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<Result> results) {
        this.results = results;
    }
}
