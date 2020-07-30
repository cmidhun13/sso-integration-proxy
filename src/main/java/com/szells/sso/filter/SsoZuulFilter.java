package com.szells.sso.filter;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SsoZuulFilter extends ZuulFilter {
 
    private Object zuulProperties;
    private final String Cookie = "cookie";
    private final String JSESSIONID = "JSESSIONID=";
    private final String XSRF_TOKEN = "XSRF-TOKEN=";
    private final String X_XSRF_TOKEN = "X-XSRF-TOKEN";
    private String JSESSIONID_VALUE = "11C07590C307F92C62AF428C15F2CD87";
    private String X_XSRF_TOKEN_VALUE = "5a9faee5-0f90-47cd-90bd-776ad4503e36";

	@Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
       HttpSession session = RequestContext.getCurrentContext().getRequest().getSession();

        ctx.getResponse().setHeader(Cookie, JSESSIONID + JSESSIONID_VALUE + ";"
                + XSRF_TOKEN + X_XSRF_TOKEN_VALUE);
        ctx.getResponse().setHeader(X_XSRF_TOKEN, X_XSRF_TOKEN_VALUE);
       /* ctx.addZuulRequestHeader("cookie", JSESSIONID + JSESSIONID_VALUE + ";"
                + XSRF_TOKEN + X_XSRF_TOKEN_VALUE);
        ctx.addZuulRequestHeader(X_XSRF_TOKEN, X_XSRF_TOKEN_VALUE);*/

        ctx.addZuulRequestHeader("firstname", "sri");
        ctx.addZuulRequestHeader("lastname", "sri");
        ctx.addZuulRequestHeader("email", "sriram@jobuli.in");
        ctx.addZuulRequestHeader("username", "sri");
        ctx.addZuulRequestHeader("secure_key", "secure");
        ctx.addZuulRequestHeader("groups", "site_author");


        // ctx.set("requestURI", "http://ec2-13-59-46-60.us-east-2.compute.amazonaws.com:8080/studio");
        // ctx.set("requestURI", "/");
        // ctx.setDebugRouting(true);
        
        // ctx.setRouteHost(new URL("http://ec2-13-59-46-60.us-east-2.compute.amazonaws.com"));
		
        // String url = UriComponentsBuilder.fromHttpUrl("http://ec2-13-59-46-60.us-east-2.compute.amazonaws.com").path("/studio")
		//								.build()
		//								.toUriString();
        
		// ctx.set("requestURI", url);
        
        // get current ZuulRoute
        // final String proxy = (String) ctx.get(FilterConstants.PROXY_KEY);
        // final ZuulRoute zuulRoute = this.zuulProperties.getRoutes().get(proxy);
	
        // patch URL by prefixing it with zuulRoute.url
        
        // final Object originalRequestPath = ctx.get(FilterConstants.REQUEST_URI_KEY);
        // final String modifiedRequestPath = zuulRoute.getUrl() + originalRequestPath;
        // context.put(FilterConstants.REQUEST_URI_KEY, modifiedRequestPath);
        
        
        return null;
    }
 
    @Override
    public boolean shouldFilter() {
       return true;
    }

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.SEND_FORWARD_FILTER_ORDER;
	}

}

