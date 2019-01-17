package com.cox.restClientApp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetDealers {
    private List<Dealer> dealers = new ArrayList<>();

    public void setDealers(List<Dealer> dealers) {
        this.dealers = dealers;
    }

    public List<Dealer> getDealers() {
        return dealers;
    }
}
