/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Since the Jetty CGI Servlet handles requests awkwardly when it is contained
 * in a web app context, this filter modifies the request so that the CGI
 * servlet can properly handle it
 *
 * @author jleonard
 */
public class ModifyCgiRequestFilter implements Filter {

    /**
     * The wrapper around CGI requests that corrects the request to produce
     * expected CGI output
     */
    private class ModifiedCgiRequest extends HttpServletRequestWrapper {

        private final HttpServletRequest originalRequest;

        /**
         * Constructor
         *
         * @param request The request to wrap
         */
        public ModifiedCgiRequest(HttpServletRequest request) {
            super(request);
            originalRequest = request;
        }

        @Override
        public String getContextPath() {
            if (originalRequest.getPathInfo() != null) {
                return originalRequest.getServletPath();
            } else {
                return originalRequest.getContextPath();
            }
        }

        @Override
        public String getServletPath() {
            if (originalRequest.getPathInfo() != null) {
                return originalRequest.getPathInfo();
            } else {
                return originalRequest.getContextPath();
            }
        }

        @Override
        public String getPathInfo() {
            return null;
        }
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
        fc.doFilter(new ModifiedCgiRequest((HttpServletRequest) sr), sr1);
    }

    @Override
    public void destroy() {
    }
}
