/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server;

import net.customware.gwt.dispatch.server.guice.ServerDispatchModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * This is the bootstrap listener loaded when servlet is initialized
 * (refer to web.xml)
 * @author iapostolos and cragusa
 * 
 */
public class BootstrapListener extends GuiceServletContextListener {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(BootstrapListener.class);

    static {
    	logger.info("BootstrapListener loaded");
    }
    
    /* (non-Javadoc)
     * @see com.google.inject.servlet.GuiceServletContextListener#getInjector()
     */
    @Override
    protected Injector getInjector() {
    	
    	logger.info("BootstrapListener getInjector");
    	
    	//maps commands to handlers
    	ActionsModule actionsModule = new ActionsModule();
    	
    	DispatchServletModule dispatchServletModule = new DispatchServletModule();
    	
        return Guice.createInjector(new ServerDispatchModule(), actionsModule, dispatchServletModule);
    }

}
