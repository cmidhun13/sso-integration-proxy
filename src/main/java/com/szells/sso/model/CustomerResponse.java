package com.szells.sso.model;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class CustomerResponse {
    public CustomerResponse(){
        super();
    }
    public Customer getCustomer() {
        return customer;
    }
    @JsonProperty("customer")
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public CustomerResponse(Customer customer) {
        this.customer = customer;
    }

    private Customer customer;
}
