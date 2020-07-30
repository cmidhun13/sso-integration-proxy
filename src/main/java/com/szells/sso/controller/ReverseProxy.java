package com.szells.sso.controller;

import com.szells.sso.model.CustomerHeaders;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.HttpCookie;
import java.util.*;

@Component
public class ReverseProxy {
    private static final Logger logger = LoggerFactory.getLogger(ReverseProxy.class);
    private HttpClient proxyClient = null;
    boolean debugMode = false;

    // cookie mangle prefix
    String PROXIED_COOKIE_MANGLE_PREFIX = "!Proxy!CrafterStudio";

    // crafter
     // String TARGET_SERVER_HOST = "ec2-34-238-193-218.compute-1.amazonaws.com";
      //String TARGET_SERVER_URL = "";
      // int TARGET_SERVER_PORT = 80;
     // String TARGET_SERVER_PROTOCOL = "http";

    // szells
   String TARGET_SERVER_HOST = "ec2-13-59-46-60.us-east-2.compute.amazonaws.com";
   String TARGET_SERVER_URL = "";
   int TARGET_SERVER_PORT = 8080;
  String TARGET_SERVER_PROTOCOL = "http";


    public void process(HttpServletRequest request, HttpServletResponse response, CustomerHeaders cusHeaders) {
        HttpResponse proxyResponse = null;
        HttpRequest proxyRequest = null;
        try {
            // Make the Request
            String method = request.getMethod();

            String proxyRequestUri = rewriteUrlFromRequest(request);

            if (debugMode || !proxyRequestUri.contains("/studio/static-assets")) {
                logger.info("Proxy to : " + proxyRequestUri);
            }

            HttpParams hcParams = new BasicHttpParams();
            hcParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
            hcParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false); // See #70
            readConfigParam(hcParams, ClientPNames.HANDLE_REDIRECTS, Boolean.class);
            proxyClient = createHttpClient(hcParams);

            //spec: RFC 2616, sec 4.3: either of these two headers signal that there is a message body.
            if (request.getHeader(HttpHeaders.CONTENT_LENGTH) != null
                    || request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {

                proxyRequest = newProxyRequestWithEntity(method, proxyRequestUri, request);
            } else {
                 proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
            }

      /*      proxyRequest.addHeader("firstname", "admin");
            proxyRequest.addHeader("lastname", "sri");
            proxyRequest.addHeader("email", "sriram@jobuli.in");
            proxyRequest.addHeader("username", "admin");
            proxyRequest.addHeader("secure_key", "secure");
            proxyRequest.addHeader("groups", "system_admin");*/
            if(cusHeaders!=null) {
                proxyRequest.addHeader("firstname", cusHeaders.getFirstName());
                proxyRequest.addHeader("lastname", cusHeaders.getLastName());
                proxyRequest.addHeader("email", cusHeaders.getEmail());
                proxyRequest.addHeader("username", cusHeaders.getUserName());
                proxyRequest.addHeader("secure_key", cusHeaders.getSecureKey());
                proxyRequest.addHeader("groups", cusHeaders.getGroups());
            }
            copyRequestHeaders(request, proxyRequest);

            setXForwardedForHeader(request, proxyRequest);

            /* make sure the X-XSRF token is in the request to proxy */
            proxyRequest.removeHeaders("X-XSRF-TOKEN");
            String cookiesValue = null;

            Header[] cookieHeaders = proxyRequest.getHeaders("COOKIE");
            if (cookieHeaders != null && cookieHeaders.length > 0) {
                cookiesValue = cookieHeaders[0].getValue();
            }

            if (cookiesValue != null) {
                String[] cookies = cookiesValue.split(";");
                for (int i = 0; i < cookies.length; i++) {
                    String part = cookies[i];
                    String parts[] = part.split("=");
                    if ("XSRF-TOKEN".equals(parts[0])) {
                        proxyRequest.setHeader("X-XSRF-TOKEN", parts[1]);
                    }
                }
            }

            // dump out the headers being sent to the remote server
            if (debugMode || !proxyRequestUri.contains("/studio/static-assets")) {
                logger.info("HeadersSentIn:");
                Header[] requestheaders = proxyRequest.getAllHeaders();
                for(Header requestheader : requestheaders) {
                    logger.info("IN ---> " + requestheader.getName() + " : " + requestheader.getValue());
                }
            }

            proxyResponse = proxyClient.execute(getTargetHost(request), proxyRequest);



            // Process the response:
            // Pass the response code. This method with the "reason phrase" is deprecated but it's the
            //   only way to pass the reason along too.
            int statusCode = proxyResponse.getStatusLine().getStatusCode();

            //noinspection deprecation
            response.setStatus(statusCode, proxyResponse.getStatusLine().getReasonPhrase());


            // Copying response headers to make sure SESSIONID or other Cookie which comes from the remote
            // server will be saved in client when the proxied url was redirected to another one.
            // See issue [#51](https://github.com/mitre/HTTP-Proxy-Servlet/issues/51)
            copyResponseHeaders(proxyResponse, request, response);

            if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
                // 304 needs special handling.  See:
                // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
                // Don't send body entity/content!
                response.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
            } else {
                // Send the content to the client
                copyResponseEntity(proxyResponse, response, proxyRequest, request);
            }

            // dump out the headers returned in the response to the browser
            if (debugMode || !proxyRequestUri.contains("/studio/static-assets")) {
                logger.info("----------------------------");
                logger.info("HeadersSentOut:");
                Collection<String> responseHeadernames = response.getHeaderNames();
                for(String responseHeader : responseHeadernames) {
                    logger.info("OUT ---> " + responseHeader + " : " + response.getHeader(responseHeader));
                }
            }

        } catch (Exception e) {
            if (proxyResponse != null)
                consumeQuietly(proxyResponse.getEntity());
        } finally {
            // make sure the entire entity was consumed, so the connection is released
            if (proxyResponse != null)
                consumeQuietly(proxyResponse.getEntity());
            //Note: Don't need to close servlet outputStream:
            // http://stackoverflow.com/questions/1159168/should-one-call-close-on-httpservletresponse-getoutputstream-getwriter
        }
    }

    private HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri,
                                                  HttpServletRequest servletRequest)
            throws IOException {
        HttpEntityEnclosingRequest eProxyRequest =
                new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
        // Add the input entity (streamed)
        //  note: we don't bother ensuring we close the servletInputStream since the container handles it
        eProxyRequest.setEntity(
                new InputStreamEntity(servletRequest.getInputStream(), servletRequest.getContentLength()));
        return eProxyRequest;
    }

    protected void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * HttpClient v4.1 doesn't have the
     * {@link EntityUtils#consumeQuietly(HttpEntity)} method.
     */
    protected void consumeQuietly(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {//ignore
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * Copy request headers from the servlet client to the proxy request.
     */
    protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {

        // Get an Enumeration of all of the header names sent by the client
        Enumeration enumerationOfHeaderNames = servletRequest.getHeaderNames();

        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = (String) enumerationOfHeaderNames.nextElement();
            copyRequestHeader(servletRequest, proxyRequest, headerName);
        }


    }

    /**
     * Copy a request header from the servlet client to the proxy request.
     * This is easily overwritten to filter out certain headers if desired.
     */
    protected void copyRequestHeader(HttpServletRequest servletRequest, HttpRequest proxyRequest, String headerName) {

        HeaderGroup hopByHopHeaders;
        hopByHopHeaders = new HeaderGroup();
        //Comment the Request headers
        String[] xheaders = new String[8];
        xheaders[0] = "Connection";
        xheaders[1] = "Keep-Alive";
        xheaders[2] = "Proxy-Authenticate";
        xheaders[3] = "Proxy-Authorization";
        xheaders[4] = "TE";
        xheaders[5] = "Trailers";
        xheaders[6] = "Transfer-Encoding";
        xheaders[7] = "Upgrade";
        //  xheaders[8] = "Cookie";

        for (String xheader : xheaders) {
            hopByHopHeaders.addHeader(new BasicHeader(xheader, null));
        }


        //Instead the content-length is effectively set via InputStreamEntity
        if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH))
            return;
        if (hopByHopHeaders.containsHeader(headerName))
            return;

        Enumeration headers = servletRequest.getHeaders(headerName);

        while (headers.hasMoreElements()) {

            //sometimes more than one value
            String headerValue = (String) headers.nextElement();

            // In case the proxy host is running multiple virtual servers,
            // rewrite the Host header to ensure that we get content from
            // the correct virtual server

            if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                HttpHost host = getTargetHost(servletRequest);
                headerValue = host.getHostName();
                //  if (host.getPort() != -1)
                //    headerValue += ":"+host.getPort();
            } else if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.COOKIE)) {
                headerValue = getRealCookie(headerValue);
            }
            // else if (headerName.equalsIgnoreCase("crafterSite")) {
            //    headerValue = getRealCookie(headerValue);
            //}
            //else if (headerName.equalsIgnoreCase("XSRF-TOKEN")) {
            //   headerValue = getRealCookie(headerValue);
            //
            //}
            //else if (headerName.equalsIgnoreCase("x-xsrf-token")) {
            //    headerValue = getRealCookie(headerValue);
            // }

            proxyRequest.addHeader(headerName, headerValue);
        }


    }

    private void setXForwardedForHeader(HttpServletRequest servletRequest,
                                        HttpRequest proxyRequest) {
        if (true) {
            String headerName = "X-Forwarded-For";
            String newHeader = servletRequest.getRemoteAddr();
            String existingHeader = servletRequest.getHeader(headerName);
            if (existingHeader != null) {
                newHeader = existingHeader + ", " + newHeader;
            }
            proxyRequest.setHeader(headerName, newHeader);
        }
    }

    /**
     * Copy proxied response headers back to the servlet client.
     */
    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            copyResponseHeader(servletRequest, servletResponse, header);
        }
    }

    /**
     * Copy a proxied response header back to the servlet client.
     * This is easily overwritten to filter out certain headers if desired.
     */
    protected void copyResponseHeader(HttpServletRequest servletRequest,
                                      HttpServletResponse servletResponse, Header header) {


        HeaderGroup hopByHopHeaders;
        hopByHopHeaders = new HeaderGroup();

        String[] xheaders = new String[8];
        xheaders[0] = "Connection";
        xheaders[1] = "Keep-Alive";
        xheaders[2] = "Proxy-Authenticate";
        xheaders[3] = "Proxy-Authorization";
        xheaders[4] = "TE";
        xheaders[5] = "Trailers";
        xheaders[6] = "Transfer-Encoding";
        xheaders[7] = "Upgrade";
       /* xheaders[8] = "crafterSite";
        xheaders[9] = "XSRF-TOKEN";
        xheaders[10] = "x-xsrf-token";
        xheaders[11] = "Cookie";
*/
        for (String xheader : xheaders) {
            hopByHopHeaders.addHeader(new BasicHeader(xheader, null));
        }


        String headerName = header.getName();
        if (hopByHopHeaders.containsHeader(headerName))
            return;
        String headerValue = header.getValue();
        if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE) ||
                headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE2)) {
            copyProxyCookie(servletRequest, servletResponse, headerValue);
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
            // LOCATION Header may have to be rewritten.
            servletResponse.addHeader(headerName, rewriteUrlFromResponse(servletRequest, headerValue));
        } else {
            servletResponse.addHeader(headerName, headerValue);
        }
    }

    /**
     * Copy cookie from the proxy to the servlet client.
     * Replaces cookie path to local path and renames cookie to avoid collisions.
     */
    protected void copyProxyCookie(HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse, String headerValue) {
        List<HttpCookie> cookies = HttpCookie.parse(headerValue);

        // Russ 07/14/2020 Why would we tie the cookie to the context path
        //String path = servletRequest.getContextPath(); // path starts with / or is empty string
        //path += servletRequest.getServletPath(); // servlet path starts with / or is empty string
        String path = "/";

        for (HttpCookie cookie : cookies) {
            //set cookie name prefixed w/ a proxy value so it won't collide w/ other cookies
            String proxyCookieName = getCookieNamePrefix() + cookie.getName();

            Cookie servletCookie = new Cookie(proxyCookieName, cookie.getValue());
            servletCookie.setComment(cookie.getComment());
            servletCookie.setMaxAge((int) cookie.getMaxAge());
            servletCookie.setPath(path); //set to the path of the proxy servlet
            // don't set cookie domain
            servletCookie.setSecure(cookie.getSecure());
            servletCookie.setVersion(cookie.getVersion());

            servletResponse.addCookie(servletCookie);
        }
    }

    /**
     * Take any client cookies that were originally from the proxy and prepare them to send to the
     * proxy.  This relies on cookie headers being set correctly according to RFC 6265 Sec 5.4.
     * This also blocks any local cookies from being sent to the proxy.
     */
    protected String getRealCookie(String cookieValue) {

        StringBuilder escapedCookie = new StringBuilder();

        String[] cookies = cookieValue.split("; ");

        for (String cookie : cookies) {

            String[] cookieSplit = cookie.split("=");

            if (cookieSplit.length == 2) {
                String cookieName = cookieSplit[0];

                if (cookieName.startsWith(getCookieNamePrefix())) {

                    cookieName = cookieName.substring(getCookieNamePrefix().length());
                    if (escapedCookie.length() > 0) {
                        escapedCookie.append("; ");
                    }

                    // cookies set by browser don't go through the proxy so we're catching this case here
                    // and esentially trusting crafterSite over the mangled value
                    if(cookieName.contains("crafterSite")) {
                        String crafterSiteValue = getCookieValue("crafterSite", cookies);
                        escapedCookie.append(cookieName).append("=").append( crafterSiteValue );
                    }
                    else {
                        escapedCookie.append(cookieName).append("=").append(cookieSplit[1]);
                    }

                }
            }

            cookieValue = escapedCookie.toString();
        }

        return cookieValue;
    }

    String getCookieValue(String CookieName, String[] cookies) {
        String value = null;

        for (int i = 0; i < cookies.length; i++) {
            String part = cookies[i];
            String parts[] = part.split("=");
            if (CookieName.compareToIgnoreCase(parts[0]) == 0) {
                value =  parts[1];
            }
        }

        return value;
    }

    /**
     * The string prefixing rewritten cookies.
     */
    protected String getCookieNamePrefix() {
        return PROXIED_COOKIE_MANGLE_PREFIX;
    }

    /**
     * Copy response body data (the entity) from the proxy to the servlet client.
     */
    protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse,
                                      HttpRequest proxyRequest, HttpServletRequest servletRequest)
            throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            OutputStream servletOutputStream = servletResponse.getOutputStream();
            entity.writeTo(servletOutputStream);
        }
    }

    /**
     * Reads the request URI from {@code servletRequest} and rewrites it, considering targetUri.
     * It's used to make the new request.
     */
    protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
        StringBuilder uri = new StringBuilder(500);
        uri.append(getTargetUri(servletRequest));
        // Handle the path given to the servlet
        if (servletRequest.getRequestURI()!= null) {//ex: /my/path.html
            //uri.append(encodeUriQuery(servletRequest.getPathInfo()));
            uri.append(encodeUriQuery(servletRequest.getRequestURI()));
        }
        // Handle the query string & fragment
        String queryString = servletRequest.getQueryString();//ex:(following '?'): name=value&foo=bar#fragment
        String fragment = null;
        //split off fragment from queryString, updating queryString if found
        if (queryString != null) {
            int fragIdx = queryString.indexOf('#');
            if (fragIdx >= 0) {
                fragment = queryString.substring(fragIdx + 1);
                queryString = queryString.substring(0, fragIdx);
            }
        }

        queryString = rewriteQueryStringFromRequest(servletRequest, queryString);
        if (queryString != null && queryString.length() > 0) {
            uri.append('?');
            uri.append(encodeUriQuery(queryString));
        }

        if (true && fragment != null) {
            uri.append('#');
            uri.append(encodeUriQuery(fragment));
        }
        return uri.toString();
    }

    protected String rewriteQueryStringFromRequest(HttpServletRequest servletRequest, String queryString) {
        return queryString;
    }

    /**
     * For a redirect response from the target server, this translates {@code theUrl} to redirect to
     * and translates it to one the original client can use.
     */
    protected String rewriteUrlFromResponse(HttpServletRequest servletRequest, String theUrl) {
        //TODO document example paths
        final String targetUri = getTargetUri(servletRequest);
        if (theUrl.startsWith(targetUri)) {
            /*-
             * The URL points back to the back-end server.
             * Instead of returning it verbatim we replace the target path with our
             * source path in a way that should instruct the original client to
             * request the URL pointed through this Proxy.
             * We do this by taking the current request and rewriting the path part
             * using this servlet's absolute path and the path from the returned URL
             * after the base target URL.
             */
            StringBuffer curUrl = servletRequest.getRequestURL();//no query
            int pos;
            // Skip the protocol part
            if ((pos = curUrl.indexOf("://")) >= 0) {
                // Skip the authority part
                // + 3 to skip the separator between protocol and authority
                if ((pos = curUrl.indexOf("/", pos + 3)) >= 0) {
                    // Trim everything after the authority part.
                    curUrl.setLength(pos);
                }
            }
            // Context path starts with a / if it is not blank
            curUrl.append(servletRequest.getContextPath());
            // Servlet path starts with a / if it is not blank
            curUrl.append(servletRequest.getServletPath());
            curUrl.append(theUrl, targetUri.length(), theUrl.length());
            theUrl = curUrl.toString();
        }


        return theUrl;
    }

    private String getTargetUri(HttpServletRequest request) {
        // make this configurable
        return TARGET_SERVER_URL;
    }

    private HttpHost getTargetHost(HttpServletRequest request) {
        // make this configurable
        return new HttpHost(TARGET_SERVER_HOST, TARGET_SERVER_PORT, TARGET_SERVER_PROTOCOL); // crafter
    }


    static CharSequence encodeUriQuery(CharSequence input) {
        BitSet asciiQueryChars;
        char[] c_unreserved = "_-!.~'()*".toCharArray();//plus alphanum
        char[] c_punct = ",;:\\$&+=".toCharArray();
        char[] c_reserved = "?/[]@".toCharArray();//plus punct
        asciiQueryChars = new BitSet(128);

        for (char c = 'a'; c <= 'z'; c++) asciiQueryChars.set((int) c);
        for (char c = 'A'; c <= 'Z'; c++) asciiQueryChars.set((int) c);
        for (char c = '0'; c <= '9'; c++) asciiQueryChars.set((int) c);
        for (char c : c_unreserved) asciiQueryChars.set((int) c);
        for (char c : c_punct) asciiQueryChars.set((int) c);
        for (char c : c_reserved) asciiQueryChars.set((int) c);

        asciiQueryChars.set((int) '%');//leave existing percent escapes in place


        //Note that I can't simply use URI.java to encode because it will escape pre-existing escaped things.
        StringBuilder outBuf = null;
        Formatter formatter = null;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            boolean escape = true;
            if (c < 128) {
                if (asciiQueryChars.get((int) c)) {
                    escape = false;
                }
            } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {//not-ascii
                escape = false;
            }
            if (!escape) {
                if (outBuf != null)
                    outBuf.append(c);
            } else {
                //escape
                if (outBuf == null) {
                    outBuf = new StringBuilder(input.length() + 5 * 3);
                    outBuf.append(input, 0, i);
                    formatter = new Formatter(outBuf);
                }
                //leading %, 0 padded, width 2, capital hex
                formatter.format("%%%02X", (int) c);//TODO
            }
        }
        return outBuf != null ? outBuf : input;
    }

    private HttpClient createHttpClient(HttpParams hcParams) {
        try {
            //as of HttpComponents v4.2, this class is better since it uses System
            // Properties:
            Class clientClazz = Class.forName("org.apache.http.impl.client.SystemDefaultHttpClient");
            Constructor constructor = clientClazz.getConstructor(HttpParams.class);
            return (HttpClient) constructor.newInstance(hcParams);
        } catch (ClassNotFoundException e) {
            //no problem; use v4.1 below
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Fallback on using older client:
        return new DefaultHttpClient(new ThreadSafeClientConnManager(), hcParams);
    }

    /**
     * The http client used.
     *
     * @see #createHttpClient(HttpParams)
     */
    private HttpClient getProxyClient() {
        return proxyClient;
    }

    /**
     * Reads a servlet config parameter by the name {@code hcParamName} of type {@code type}, and
     * set it in {@code hcParams}.
     */
    private void readConfigParam(HttpParams hcParams, String hcParamName, Class type) {
        String val_str = ""; //RUSS COMMENTED THIS OUT NEED TO KNOW WHAT THESE ARE IN CONFIG getConfigParam(hcParamName);
        if (val_str == null)
            return;
        Object val_obj;
        if (type == String.class) {
            val_obj = val_str;
        } else {
            try {
                //noinspection unchecked
                val_obj = type.getMethod("valueOf", String.class).invoke(type, val_str);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        hcParams.setParameter(hcParamName, val_obj);
    }
}
