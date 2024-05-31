/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;


import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The GwtCacheControlFilter is used to prevent browser caching of the gwt *.nocache.* files.
 * This mechanism is described here:
 *    http://www.gwtproject.org/doc/latest/DevGuideCompilingAndDebugging.html#perfect_caching
 * 
 * Servlets that run GWT can add this filter in their web.xml file to ensure that the browsers
 * do not cache the *.nocache.* files.
 * 
 * 
 * 
 * Adapted from:  
 *   http://www.gwtplayground.com/2012/09/caching-browser-scripts-in-gwt.html
 * 
 * @author nblomberg
 *
 */
public class GwtCacheControlFilter implements Filter {

    @Override
    public void destroy() {
        
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        if(requestURI.contains(".nocache.")) {
           Date date = new Date();              
           HttpServletResponse httpResponse = (HttpServletResponse)response;
           httpResponse.setDateHeader("Date", date.getTime());
           
           // This sets the date to 1 day old.  (current time - number of millsecs in a day)
           httpResponse.setDateHeader("Expires", date.getTime() - 86400000L);
           // Setting pragma here in a response is not "per spec" but some user agents may respond to it.
           //   https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Avoiding_caching
           httpResponse.setHeader("Pragma", "no-cache");
           // Gwt documentation suggests to only use: 
           //   Cache-Control "public, max-age=0, must-revalidate"
           // However it appears in Jetty other users use "no-cache, no-store" options instead.
           httpResponse.setHeader("Cache-control", "no-cache, no-store, max-age=0, must-revalidate");
        }
        filterChain.doFilter(request, response);
        
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
       
        
    }
    
    
}
