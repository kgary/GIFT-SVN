/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.ImageProperties;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchGatServerProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchGatServerPropertiesResult;
import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Contains the server side logic to retrieve the Gat Server Properties.  
 * The ServerProperties allows for server defined values to be passed to the client at runtime.
 * New properties can be added as needed via the SERVER_PROPERTIES.addPoperty method (see below).
 * 
 * @author nblomberg
 *
 */
public class FetchGatServerPropertiesHandler implements ActionHandler<FetchGatServerProperties, FetchGatServerPropertiesResult> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FetchGatServerPropertiesHandler.class);
    
    /** capture important properties that the gat client may need */
    private static ServerProperties SERVER_PROPERTIES = new ServerProperties();
    
    static{
        SERVER_PROPERTIES.addProperty(ServerProperties.DEPLOYMENT_MODE, DashboardProperties.getInstance().getDeploymentMode().getName());
        SERVER_PROPERTIES.addProperty(ServerProperties.GAT_URL, DashboardProperties.getInstance().getGiftAuthorToolURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.USE_HTTPS, Boolean.toString(DashboardProperties.getInstance().shouldUseHttps()));
        SERVER_PROPERTIES.addProperty(ServerProperties.DOMAIN_CONTENT_SERVER_ADDRESS, DomainModuleProperties.getInstance().getDomainContentServerAddress());
        SERVER_PROPERTIES.addProperty(ServerProperties.ASAT_URL, DashboardProperties.getInstance().getASATURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.AUTHORITATIVE_SYSTEM_URL, DashboardProperties.getInstance().getAuthoritativeSystemUrl());
        SERVER_PROPERTIES.addProperty(ServerProperties.BYPASS_SURVEY_PERMISSION_CHECK, Boolean.toString(DashboardProperties.getInstance().getByPassSurveyPermissionCheck()));
        SERVER_PROPERTIES.addProperty(ServerProperties.TRUSTED_LTI_CONSUMERS, DashboardProperties.getInstance().getPropertyValue(DashboardProperties.TRUSTED_LTI_CONSUMERS));
        SERVER_PROPERTIES.addProperty(ServerProperties.TRUSTED_LTI_PROVIDERS, DashboardProperties.getInstance().getPropertyValue(DashboardProperties.TRUSTED_LTI_PROVIDERS));
        SERVER_PROPERTIES.addProperty(ServerProperties.DASHBOARD_URL, DashboardProperties.getInstance().getDashboardURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.LTI_URL, DashboardProperties.getInstance().getLtiServletUrl());
        SERVER_PROPERTIES.addProperty(ServerProperties.LTI_CONSUMER_URL, DashboardProperties.getInstance().getLtiConsumerServletUrl());
        SERVER_PROPERTIES.addProperty(ServerProperties.LTI_HELP_PAGE_URL, HelpDocuments.getLtiDoc().toString());
        SERVER_PROPERTIES.addProperty(ServerProperties.VERSION_NAME, Version.getInstance().getName());
        SERVER_PROPERTIES.addProperty(ServerProperties.VERSION_DATE, Version.getInstance().getReleaseDate());
        SERVER_PROPERTIES.addProperty(ServerProperties.BUILD_DATE, Version.getInstance().getBuildDate());
        SERVER_PROPERTIES.addProperty(ServerProperties.BUILD_LOCATION, Version.getInstance().getBuildLocation());
        SERVER_PROPERTIES.addProperty(ServerProperties.DOCUMENTATION_TOKEN, Version.getInstance().getDocumentationToken()); 
        SERVER_PROPERTIES.addProperty(ServerProperties.DEFAULT_MBP_RECALL_ALLOWED_ATTEMPTS, Integer.toString(MerrillsBranchPointHandler.DEFAULT_RECALL_BAILOUT_CNT));
        SERVER_PROPERTIES.addProperty(ServerProperties.DEFAULT_MBP_PRACTICE_ALLOWED_ATTEMPTS, Integer.toString(MerrillsBranchPointHandler.DEFAULT_PRACTICE_BAILOUT_CNT));
        SERVER_PROPERTIES.addProperty(ServerProperties.TUI_URL, DashboardProperties.getInstance().getTutorURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.DEFAULT_CHARACTER, DashboardProperties.getInstance().getDefaultCharacterPath());
        SERVER_PROPERTIES.addProperty(ServerProperties.GOOGLE_MAPS_API_KEY, DashboardProperties.getInstance().getPropertyValue(DashboardProperties.GOOGLE_MAPS_API_KEY));
        SERVER_PROPERTIES.addProperty(ServerProperties.LESSON_LEVEL, DashboardProperties.getInstance().getPropertyValue(DashboardProperties.LESSON_LEVEL));
        SERVER_PROPERTIES.addProperty(ServerProperties.JRE_BIT, Boolean.toString(DashboardProperties.getInstance().isJRE64Bit()));
        SERVER_PROPERTIES.addProperty(ServerProperties.EXTERNAL_STRATEGY_PROVIDER_URL, DashboardProperties.getInstance().getExternalStrategyProviderUrl());
        
        // IMAGES
        SERVER_PROPERTIES.addProperty(ServerProperties.BACKGROUND_IMAGE, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.BACKGROUND));
        SERVER_PROPERTIES.addProperty(ServerProperties.ORGANIZATION_IMAGE, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.ORGANIZATION_IMAGE));
        SERVER_PROPERTIES.addProperty(ServerProperties.SYSTEM_ICON_SMALL, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.SYSTEM_ICON_SMALL));
    }
    
    @Override
    public FetchGatServerPropertiesResult execute(FetchGatServerProperties action, ExecutionContext context)
            throws DispatchException {
        long start = System.currentTimeMillis();
        logger.info("execute() called with action: " + action);
        
        FetchGatServerPropertiesResult result = new FetchGatServerPropertiesResult(SERVER_PROPERTIES);
        MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchGatServerProperties", start);
        return result;
    }

    @Override
    public Class<FetchGatServerProperties> getActionType() {
        return FetchGatServerProperties.class;
    }

    @Override
    public void rollback(FetchGatServerProperties action, FetchGatServerPropertiesResult result, ExecutionContext context)
            throws DispatchException {
        
        // Do nothing.
        
    }

   
}
