package com.szells.sso.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Customer {

    public Customer(String customerId, String userId, String buisnessName, String businessEmail, String firstName, String lastName) {
        this.customerId = customerId;
        this.userId = userId;
        this.buisnessName = buisnessName;
        this.businessEmail = businessEmail;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Customer(){
        super();
    }


    public String getCustomerId() {
        return customerId;
    }
    @JsonProperty("customerId")
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBuisnessName() {
        return buisnessName;
    }
    @JsonProperty("businessName")
    public void setBuisnessName(String buisnessName) {
        this.buisnessName = buisnessName;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("businessName")
    private String buisnessName;

    @JsonProperty("businessEmail")
    private String businessEmail;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;







}
