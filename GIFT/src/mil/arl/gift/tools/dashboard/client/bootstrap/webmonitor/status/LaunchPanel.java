/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.status;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleStateProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleStateProvider.ModuleStatusChangeHandler;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.WebMonitorStatusProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.WebMonitorStatusProvider.WebMonitorStatusChangeHandler;
import mil.arl.gift.tools.dashboard.shared.rpcs.WebMonitorStatus;

/**
 * A panel used to launch GIFT modules from the web monitor
 * 
 * @author nroberts
 */
public class LaunchPanel extends Composite implements ModuleStatusChangeHandler, WebMonitorStatusChangeHandler{
    
    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(LaunchPanel.class.getName());

    private static LaunchPanelUiBinder uiBinder = GWT.create(LaunchPanelUiBinder.class);

    interface LaunchPanelUiBinder extends UiBinder<Widget, LaunchPanel> {
    }

    /**
     * Controls the "Launch All Modules" button. see launchButton below for control of "Launch All."
     * Starts any modules currently inactive.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchAllButton = new LaunchModuleButton(Arrays.asList(
            ModuleTypeEnum.DOMAIN_MODULE, ModuleTypeEnum.GATEWAY_MODULE, ModuleTypeEnum.LEARNER_MODULE,
            ModuleTypeEnum.LMS_MODULE, ModuleTypeEnum.PEDAGOGICAL_MODULE,
            ModuleTypeEnum.SENSOR_MODULE, ModuleTypeEnum.TUTOR_MODULE, ModuleTypeEnum.UMS_MODULE));
    
    /**
     * Controls "Kill All Modules" button. Stops any currently active modules. 
     */
    @UiField(provided=true)
    protected KillModuleButton killAllButton = new KillModuleButton(Arrays.asList(
            ModuleTypeEnum.DOMAIN_MODULE, ModuleTypeEnum.GATEWAY_MODULE, ModuleTypeEnum.LEARNER_MODULE,
            ModuleTypeEnum.LMS_MODULE, ModuleTypeEnum.PEDAGOGICAL_MODULE,
            ModuleTypeEnum.SENSOR_MODULE, ModuleTypeEnum.TUTOR_MODULE, ModuleTypeEnum.UMS_MODULE));
    
    /**
     * Launches UMS and LMS Modules.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchUMSLMSButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.UMS_MODULE, ModuleTypeEnum.LMS_MODULE));
    
    /**
     * Launches UMS Module.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchUMSButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.UMS_MODULE));
    
    /**
     * Launches LMS Modules.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchLMSButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.LMS_MODULE));
    
    
    /**
     * Controls the "Launch All" button, see LaunchAllButton above for control of "Launch All Modules"
     * This button starts all modules except UMS and LMS.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchButton = new LaunchModuleButton(Arrays.asList(
            ModuleTypeEnum.DOMAIN_MODULE, ModuleTypeEnum.GATEWAY_MODULE, ModuleTypeEnum.LEARNER_MODULE,
            ModuleTypeEnum.PEDAGOGICAL_MODULE, ModuleTypeEnum.SENSOR_MODULE, ModuleTypeEnum.TUTOR_MODULE));
    
    /**
     * Launches Sensor Module.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchSensorButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.SENSOR_MODULE));
    
    /**
     * Launches Pedagogical Module.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchPedButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.PEDAGOGICAL_MODULE));
    
    /**
     * Launches Domain module.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchDomainButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.DOMAIN_MODULE));
    
    /**
     * Launches Learner module.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchLearnerButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.LEARNER_MODULE));
    
    /**
     * Launches Tutor module.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchTutorButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.TUTOR_MODULE));
    
    /**
     * Launches Gateway module.
     */
    @UiField(provided=true)
    protected LaunchModuleButton launchGatewayButton = new LaunchModuleButton(Arrays.asList(ModuleTypeEnum.GATEWAY_MODULE));
    
    /** The label used to show the broker URL */
    @UiField
    protected Label brokerUrlLabel;
    
    /**
     * Creates a new panel with which to launch GIFT modules
     */
    public LaunchPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        redrawMonitorStatusFields();
        
        ModuleStateProvider.getInstance().addHandler(this);
        WebMonitorStatusProvider.getInstance().addHandler(this);
        
        /* Hide the button that launches the Gateway module when in server mode */
        if(DeploymentModeEnum.SERVER.equals(UiManager.getInstance().getDeploymentMode())) {
            launchGatewayButton.setVisible(false);
        }
    }

    /**
     * Redraws any fields that display the current status of the web monitor so that
     * the reflect the most recently reported status from the server
     */
    private void redrawMonitorStatusFields() {
        
        WebMonitorStatus status = WebMonitorStatusProvider.getInstance().getMonitorStatus();
        if(status == null) {
            brokerUrlLabel.setText(null);
            
        } else {
            brokerUrlLabel.setText(status.getBrokerUrl());
        }
    }

    /**
     * Implements the logic to disable/enable buttons based on the status of the related module. 
     */
    @Override
    public void onModuleStatusChanged(Map<ModuleTypeEnum, List<String>> moduleTypeToQueues) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Updating launch buttons based on module status: " + moduleTypeToQueues);
        }
        
        /* Enable/disable buttons based on which modules are running */
        boolean umsUnavailable = true;
        boolean lmsUnavailable = true;
        boolean domainUnavailable = true;
        boolean tutorUnavailable = true;
        boolean gatewayUnavailable = true;
        boolean sensorUnavailable = true;
        boolean pedUnavailable = true;
        boolean learnerUnavailable = true;
        
        boolean gatewayLaunchDisabled = DeploymentModeEnum.SERVER.equals(UiManager.getInstance().getDeploymentMode());
        
        List<String> umsQueues = moduleTypeToQueues.get(ModuleTypeEnum.UMS_MODULE);
        if(umsQueues != null && !umsQueues.isEmpty()) {
            umsUnavailable = false;
        }
        
        List<String> lmsQueues = moduleTypeToQueues.get(ModuleTypeEnum.LMS_MODULE);
        if(lmsQueues != null && !lmsQueues.isEmpty()) {
            lmsUnavailable = false;
        }
        
        List<String> domainQueues = moduleTypeToQueues.get(ModuleTypeEnum.DOMAIN_MODULE);
        if(domainQueues != null && !domainQueues.isEmpty()) {
            domainUnavailable = false;
        }
        
        List<String> tutorQueues = moduleTypeToQueues.get(ModuleTypeEnum.TUTOR_MODULE);
        if(tutorQueues != null && !tutorQueues.isEmpty()) {
            tutorUnavailable = false;
        }
        
        List<String> gatewayQueues = moduleTypeToQueues.get(ModuleTypeEnum.GATEWAY_MODULE);
        if(gatewayQueues != null && !gatewayQueues.isEmpty()) {
            gatewayUnavailable = false;
        }
        
        List<String> sensorQueues = moduleTypeToQueues.get(ModuleTypeEnum.SENSOR_MODULE);
        if(sensorQueues != null && !sensorQueues.isEmpty()) {
            sensorUnavailable = false;
        }
        
        List<String> pedQueues = moduleTypeToQueues.get(ModuleTypeEnum.PEDAGOGICAL_MODULE);
        if(pedQueues != null && !pedQueues.isEmpty()) {
            pedUnavailable = false;
        }
        
        List<String> learnerQueues = moduleTypeToQueues.get(ModuleTypeEnum.LEARNER_MODULE);
        if(learnerQueues != null && !learnerQueues.isEmpty()) {
            learnerUnavailable = false;
        }
        
        launchUMSButton.setEnabled(umsUnavailable);
        launchLMSButton.setEnabled(lmsUnavailable);
        launchDomainButton.setEnabled(domainUnavailable);
        launchTutorButton.setEnabled(tutorUnavailable);
        launchGatewayButton.setEnabled(gatewayUnavailable && !gatewayLaunchDisabled);
        launchSensorButton.setEnabled(sensorUnavailable);
        launchPedButton.setEnabled(pedUnavailable);
        launchLearnerButton.setEnabled(learnerUnavailable);
        
        launchUMSLMSButton.setEnabled(umsUnavailable || lmsUnavailable);
        
        launchButton.setEnabled(domainUnavailable
                || tutorUnavailable
                || (gatewayUnavailable && !gatewayLaunchDisabled)
                || sensorUnavailable
                || pedUnavailable
                || learnerUnavailable);
        
        launchAllButton.setEnabled(umsUnavailable 
                || lmsUnavailable
                || domainUnavailable
                || tutorUnavailable
                || (gatewayUnavailable && !gatewayLaunchDisabled)
                || sensorUnavailable
                || pedUnavailable
                || learnerUnavailable);
        
        killAllButton.setEnabled(!umsUnavailable 
                || !lmsUnavailable
                || !domainUnavailable
                || !tutorUnavailable
                || !(gatewayUnavailable && !gatewayLaunchDisabled)
                || !sensorUnavailable
                || !pedUnavailable
                || !learnerUnavailable);
    }

    @Override
    public void onMonitorStatusChanged(WebMonitorStatus status) {
        redrawMonitorStatusFields();
    }

}
