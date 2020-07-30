package com.szells.sso.controller;

import com.szells.sso.adapter.CustomerAdapter;
import com.szells.sso.converter.RedirectServletRequestWrapper;
import com.szells.sso.model.CustomerHeaders;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST})

public class RedirectFilter implements javax.servlet.Filter {
private ServletContext servletContext;
private Logger log;
private CustomerAdapter customerAdapter;

public RedirectFilter(){
        super();
}

public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
        log = Logger.getLogger(RedirectFilter.class.getName());
        }

public void doFilter( ServletRequest req,
        ServletResponse res,
        FilterChain filterChain)
        throws IOException, ServletException {

      //  RedirectServletRequestWrapper httpReq = new RedirectServletRequestWrapper((HttpServletRequest) req);
      javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest) req;

      String requestUrl =  request.getRequestURI();
      System.out.println("Request url received---- "+requestUrl);

        if(requestUrl.equals("/loginPage.html")){
            filterChain.doFilter(req,res);
    }  else {
        ReverseProxy reverseProxy = new ReverseProxy();
        CustomerHeaders cusHeaders = null;
        javax.servlet.http.HttpServletRequest wrapperRequest = (javax.servlet.http.HttpServletRequest) req;
        String cusIdAttribute = (String)req.getAttribute("customerId");
        String customerId = request.getParameter("customerId");
            if(customerId!=null){
                System.out.println("Received Customer id : "+ customerId);
                customerAdapter = new CustomerAdapter();
                Enumeration<String> params = req.getParameterNames();
                cusHeaders =  customerAdapter.findCustomerHeaders(customerId);
                wrapperRequest.setAttribute("cusHeader",cusHeaders);
            }
            HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(wrapperRequest);
            cusHeaders = (CustomerHeaders) request.getAttribute("cusHeader");
            reverseProxy.process(requestWrapper, (HttpServletResponse) res, cusHeaders);

    }

}

public void destroy(){
}
}
