/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import javax.servlet.ServletException;
import mil.arl.gift.tutor.client.TutorTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class TutorTestServiceImpl extends RemoteServiceServlet implements TutorTestService {

    /**
     * instance of the logger
     */
    private static Logger logger = LoggerFactory.getLogger(TutorTestServiceImpl.class);

    /**
     * Constructor
     */
    public TutorTestServiceImpl() {
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("Initialized.");
    }
}
