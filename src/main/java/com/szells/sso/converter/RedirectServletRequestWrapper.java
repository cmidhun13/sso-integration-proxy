package com.szells.sso.converter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class RedirectServletRequestWrapper extends HttpServletRequestWrapper {
    private Map headerMap;

    public void addHeader(String name, String value){
        headerMap.put(name, new String(value));
    }

    public RedirectServletRequestWrapper(HttpServletRequest request){
        super(request);
        headerMap = new HashMap();
    }

    public Enumeration getHeaderNames(){
        HttpServletRequest request = (HttpServletRequest)getRequest();
        List list = new ArrayList();
        for( Enumeration e = request.getHeaderNames() ;  e.hasMoreElements() ; )
            list.add(e.nextElement().toString());
        for( Iterator i = headerMap.keySet().iterator() ; i.hasNext() ; ){
            list.add(i.next());
        }
        return Collections.enumeration(list);
    }
    public String getHeader(String name){
        Object value;
        if((value = headerMap.get(""+name)) != null)
            return value.toString();
        else
            return ((HttpServletRequest)getRequest()).getHeader(name);
    }
}
