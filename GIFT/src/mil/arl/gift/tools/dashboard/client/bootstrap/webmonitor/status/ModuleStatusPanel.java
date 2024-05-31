/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleStateProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleStateProvider.ModuleStatusChangeHandler;

/**
 * A panel used to view the status of and stop GIFT's modules
 * 
 * @author nroberts
 */
public class ModuleStatusPanel extends Composite implements ModuleStatusChangeHandler {

    private static ModuleStatusPanelUiBinder uiBinder = GWT.create(ModuleStatusPanelUiBinder.class);

    interface ModuleStatusPanelUiBinder extends UiBinder<Widget, ModuleStatusPanel> {
    }
    
    /** The collapsible panel representing the UMS module */
    @UiField(provided=true)
    protected ModuleStatusCollapsePanel umsCollapse = addModuleCollapse(ModuleTypeEnum.UMS_MODULE);
    
    /** The collapsible panel representing the LMS module */
    @UiField(provided=true)
    protected ModuleStatusCollapsePanel lmsCollapse = addModuleCollapse(ModuleTypeEnum.LMS_MODULE);
    
    /** The collapsible panel representing the domain module */
    @UiField(provided=true)
    protected ModuleStatusCollapsePanel domainCollapse = addModuleCollapse(ModuleTypeEnum.DOMAIN_MODULE);
    
    /** The collapsible panel representing the tutor module */
    @UiField(provided=true)
    protected ModuleStatusCollapsePanel tutorCollapse = addModuleCollapse(ModuleTypeEnum.TUTOR_MODULE);
    
    /** The collapsible panel representing the pedagogical module */
    @UiField(provided=true)
    protected ModuleStatusCollapsePanel pedCollapse = addModuleCollapse(ModuleTypeEnum.PEDAGOGICAL_MODULE);
    
    /** The collapsible panel representing the learner module */
    @UiField(provided=true)
    protected ModuleStatusCollapsePanel learnerCollapse = addModuleCollapse(ModuleTypeEnum.LEARNER_MODULE);
    
    /** The collapsible panel representing the gateway module */
    @UiField(provided=true)
    protected ModuleStatusCollapsePanel gatewayCollapse = addModuleCollapse(ModuleTypeEnum.GATEWAY_MODULE);
    
    /** The collapsible panel representing the sensor module */
    @UiField(provided=true)
    protected ModuleStatusCollapsePanel sensorCollapse = addModuleCollapse(ModuleTypeEnum.SENSOR_MODULE);
    
    /** A mapping from each module type to its appriopriate collapse panel */
    private Map<ModuleTypeEnum, ModuleStatusCollapsePanel> moduleToCollapse;

    /**
     * Creates a new panel used to view status of and stop GIFT's modules
     */
    public ModuleStatusPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        ModuleStateProvider.getInstance().addHandler(this);
    }


    @Override
    public void onModuleStatusChanged(Map<ModuleTypeEnum, List<String>> moduleTypeToQueues) {
        
        for(ModuleTypeEnum module : moduleTypeToQueues.keySet()) {
            
            ModuleStatusCollapsePanel collapse = moduleToCollapse.get(module);
            if(collapse == null) {
                throw new IllegalStateException("Cannot update collapseable panel for " + module + "because no such"
                        + "panel was ever created");
            }
            
            collapse.setQueueNames(moduleTypeToQueues.get(module));
        }
    }
    
    /**
     * Creates and adds a new collapsible panel for the given module type
     * 
     * @param module the module to create a panel for. Cannot be null.
     * @return the panel created for the module. Will not be null;
     */
    private ModuleStatusCollapsePanel addModuleCollapse(ModuleTypeEnum module) {
        
        if(module == null) {
            throw new IllegalArgumentException("The module to create a panel for cannot be null");
        }
        
        if(moduleToCollapse == null) {
            moduleToCollapse = new HashMap<>();
        }
        
        ModuleStatusCollapsePanel collapse = new ModuleStatusCollapsePanel(module);
        moduleToCollapse.put(module, collapse);
        
        return collapse;
    }
}
