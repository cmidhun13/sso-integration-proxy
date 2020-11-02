package com.szells.sso.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.szells.sso.model.CustomerHeaders;
import com.szells.sso.model.CustomerResponse;
import com.szells.sso.model.CutomerGraphQLQuery;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;


public class CustomerAdapter {

    @Value("${url.customer.service}")
    private String customerUrl;


    public CustomerHeaders findCustomerHeaders(String customerId) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String query = null;
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/json"); // just modified graphql into json
        String URL = "http://3.15.40.28:8080/api/v1/customersWithoutAuth";

        String query1 = "{\ncustomer(id:" + "\"" + customerId + "\"){\ncustomerId\nbusinessName\nbusinessEmail\nfirstName\nlastName\n" +
                "    }\n" +
                "}";
        System.out.println("Query Used :" + query1);
        ResponseEntity<String> response = restTemplate.postForEntity(URL, new HttpEntity<>(query1, headers), String.class);
        System.out.println("Body" + response.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        CustomerResponse customerResponse = objectMapper.reader().forType(CustomerResponse.class).readValue(response.getBody());
        System.out.println("The response of customer Id =================" + customerResponse.getCustomer().getCustomerId());
        CustomerHeaders cusHeaders = null;
        if ("89".equals(customerId)){

            cusHeaders = CustomerHeaders.builder().firstName("Selva")
                    .lastName("Sakthivel")
                    .userName("admin")
                    .groups("system_admin")
                    .email(customerResponse.getCustomer().getBusinessEmail()).secureKey("secure").build();
            System.out.println("The response 89 ================="+response);
        }else{
            cusHeaders = CustomerHeaders.builder().firstName(customerResponse.getCustomer().getFirstName())
                    .lastName(customerResponse.getCustomer().getLastName())
                    .userName("admin")
                    .groups("system_admin")
                    .email(customerResponse.getCustomer().getBusinessEmail()).secureKey("secure").build();
        }
        System.out.println("The response================="+response);
        return cusHeaders;

    }

}
