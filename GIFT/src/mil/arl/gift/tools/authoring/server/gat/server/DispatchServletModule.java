/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import net.customware.gwt.dispatch.server.guice.GuiceStandardDispatchServlet;

import com.google.inject.servlet.ServletModule;

/**
 * This dispatches events to the guice injects servlet.
 * Instantiated by BoostrapListener
 * 
 * @author iapostolos
 *
 */
public class DispatchServletModule extends ServletModule {
	
    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(DispatchServletModule.class);
    /* (non-Javadoc)
     * @see com.google.inject.servlet.ServletModule#configureServlets()
     */
    @Override
    protected void configureServlets() {   
        logger.info("configureServlets()");       
        serve("/gat/dispatch").with(GuiceStandardDispatchServlet.class);     
        
        MetricsSenderSingleton.createInstance("gat");
       
        MetricsSenderSingleton.getInstance().startSending();
        
    }
}
