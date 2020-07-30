package com.szells.sso;

public class TestPayload {

    public static void main(String args[]){
        String customerId="86";
        String query1 = "{\ncustomer(id:"+"\""+customerId+"\"){\ncustomerId\nbuisnessName\nuserId\n" +
                "    }\n" +
                "}";
        System.out.println("print Query "+ query1);
    }
}
