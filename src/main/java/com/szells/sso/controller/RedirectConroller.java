package com.szells.sso.controller;

import com.szells.sso.model.CrafterLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@RestController
public class RedirectConroller {
    private final String Cookie = "cookie";
    private final String JSESSIONID = "JSESSIONID=";
    private final String XSRF_TOKEN = "XSRF-TOKEN=";
    private final String X_XSRF_TOKEN = "X-XSRF-TOKEN";
    private String JSESSIONID_VALUE = "11C07590C307F92C62AF428C15F2CD87";
    private String X_XSRF_TOKEN_VALUE = "5a9faee5-0f90-47cd-90bd-776ad4503e36";


    final static String SALTOLINEA = "\n";
    Logger log = LoggerFactory.getLogger(RedirectConroller.class);
    @RequestMapping(path = "/state/*")
    public ResponseEntity<?> test(HttpServletRequest request,HttpServletResponse httpServletResponse) throws URISyntaxException {
   /*     URI vendorLoc = new URI("http://ec2-3-19-67-124.us-east-2.compute.amazonaws.com/studio");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(vendorLoc);*/

        // authorized headers
        HttpSession session =  request.getSession();
        if(session==null){
            session = request.getSession(true);
        }

        ReverseProxy reverseProxy = new ReverseProxy();
        javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest) request;
        HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(req);
        requestWrapper.addHeader("firstname", "sri");
        requestWrapper.addHeader("lastname", "sri");
        requestWrapper.addHeader("email", "sriram@jobuli.in");
        requestWrapper.addHeader("username", "sri");
        requestWrapper.addHeader("secure_key", "secure");
        requestWrapper.addHeader("groups", "site_author");
        System.out.println("INSIDE CUSTOM FILTER END");
        httpServletResponse.setHeader("location","http://ec2-3-19-67-124.us-east-2.compute.amazonaws.com/studio");

        reverseProxy.process(requestWrapper, (HttpServletResponse) httpServletResponse,null);

        System.out.println("Redirecting to the url in the Netflix Zuul");
        return new ResponseEntity<>(requestWrapper, HttpStatus.SEE_OTHER);
        //return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);

    }
    @RequestMapping(path = "/redirect")
    public ResponseEntity<?> redirectcrafter(HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
        URI vendorLoc = new URI("http://ec2-3-19-67-124.us-east-2.compute.amazonaws.com/studio");
       HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(vendorLoc);

        // authorized headers
        httpHeaders.set("firstname", "sri");
        httpHeaders.set("lastname", "sri");
        httpHeaders.set("email", "sriram@jobuli.in");
        httpHeaders.set("username", "sri");
        httpHeaders.set("secure_key","secure");
        httpHeaders.set("groups","site_author");

        response.setHeader("firstname","sri");
        response.setHeader("lastname","sri");
        response.setHeader("username","sri");
        response.setHeader("secure_key","secure");
        response.setHeader("groups","site_author");
        response.setHeader(Cookie, JSESSIONID + JSESSIONID_VALUE + ";"
                + XSRF_TOKEN + X_XSRF_TOKEN_VALUE);
        response.setHeader(X_XSRF_TOKEN, X_XSRF_TOKEN_VALUE);

      // response.sendRedirect("http://ec2-3-19-67-124.us-east-2.compute.amazonaws.com/studio");
      return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
      //  return new ResponseEntity<?>("forward:http://ec2-3-19-67-124.us-east-2.compute.amazonaws.com/studio");

    }


    public ModelAndView redirectWithUsingForwardPrefix(ModelMap model) {
      model.addAttribute("attribute", "forwardWithForwardPrefix");
      return new ModelAndView("forward:/http://ec2-3-19-67-124.us-east-2.compute.amazonaws.com/studio", model);
     }

    /* public void getSession(){
         RestTemplate restTemplate = new RestTemplate();
         CrafterLogin login = new CrafterLogin();
         ResponseEntity<?> response =
                 restTemplate.getForEntity(
                         "http://localhost:8080/employees/",
                         .class);
         System.out.println(response.getBody());
     }*/

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userName=request.getParameter("userName");
        String newUrl = "http://ec2-3-19-67-124.us-east-2.compute.amazonaws.com/studio";


        response.setHeader("firstname","sri");
        response.setHeader("lastname","sri");
        response.setHeader("username","sri");
        response.setHeader("secure_key","secure");
        response.setHeader("groups","site_author");
        response.setHeader(Cookie, JSESSIONID + JSESSIONID_VALUE + ";"
                + XSRF_TOKEN + X_XSRF_TOKEN_VALUE);
        response.setHeader(X_XSRF_TOKEN, X_XSRF_TOKEN_VALUE);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS,POST");
        response.setHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept, X-Auth-Token, X-Csrf-Token, WWW-Authenticate, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "false");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.sendRedirect(newUrl);

    }
}



