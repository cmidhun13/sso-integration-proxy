package com.szells.sso.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
    boolean enableDebug = false;

    /**
     * construct a wrapper for this request
     * 
     * @param request
     */
    public HeaderMapRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    private Map<String, String> headerMap = new HashMap<String, String>();

    /**
     * add a header with given name and value
     * 
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        System.out.println("Name: " + name + " Value: " + value);
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = super.getHeader(name);

        if(enableDebug) {
            if (headerMap.containsKey(name)) {
                headerValue = headerMap.get(name);
            }
            System.out.println("HeaderName: " + name + " HeaderValue: " + headerValue);
            System.out.println("headerMap: " + headerMap.toString());
            System.out.println("request URL: " + getRequestURL());
        }

        return headerValue;
    }

    /**
     * get the Header names
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        for (String name : headerMap.keySet()) {
            names.add(name);
        }
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
        }
        return Collections.enumeration(values);
    }

}
