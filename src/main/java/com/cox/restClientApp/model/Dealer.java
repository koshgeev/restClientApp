package com.cox.restClientApp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dealer {
    private Integer dealerId;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    private List<Vehicle> vehicles = new ArrayList<>();

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public Integer getDealerId() {
        return dealerId;
    }

    public void setDealerId(Integer dealerId) {
        this.dealerId = dealerId;
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dealer)) return false;
        Dealer dealer = (Dealer) o;
        return Objects.equals(dealerId, dealer.dealerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dealerId);
    }

    @Override
    public String toString() {
        return "Dealer{" +
                "dealerId='" + dealerId + '\'' +
                ", dealerName='" + name + '\'' +
                '}';
    }
}
