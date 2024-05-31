/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.EntityDisplaySettingsPanel.RenderableAttribute;

/**
 * A panel that provides the user with UI controls to modify the appearance and other facets of map entities
 * 
 * @author nroberts
 */
public class EntitySettingsPanel extends Composite {

    private static EntitySettingsPanelUiBinder uiBinder = GWT.create(EntitySettingsPanelUiBinder.class);

    interface EntitySettingsPanelUiBinder extends UiBinder<Widget, EntitySettingsPanel> {
    }
    
    /** The header that can be clicked on to show and hide this panel's contents */
    @UiField
    protected Widget headerPanel;
    
    /** The tooltip used to provide the user with instructions on how to show and hide this panel's contents */
    @UiField
    protected Tooltip tooltip;
    
    /** The header text shown above this panel's content */
    @UiField
    protected Widget headerText;
    
    /** The collapse used to show/hide this panel's content*/
    @UiField
    protected Collapse bodyCollapse;
    
    /** A panel containing settings to control how entities are displayed */
    @UiField(provided=true)
    protected EntityDisplaySettingsPanel entityDisplaySettings = new EntityDisplaySettingsPanel(this);
    
    /** Whether this panel's contents are currently showing */
    private boolean isBodyVisible = false;
    
    /** A command to invoke whenever one of the configurable settings provided by this panel is changed*/
    private Command settingChangedCommand = null;

    /** 
     * Creates a new settings panel with the default settings 
     */
    public EntitySettingsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        headerPanel.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                isBodyVisible = !isBodyVisible;
                
                bodyCollapse.toggle();
                headerText.setVisible(isBodyVisible);
                
                tooltip.hide();
            }
            
        }, ClickEvent.getType());
    }
    
    /**
     * Gets whether the user's current settings specify that the given attribute should be displayed
     * 
     * @param attribute the renderable attribute to be displayed
     * @return whether or not the attribute should be displayed according to the user's settings
     */
    public boolean shouldDisplayAttribute(RenderableAttribute attribute) {
        return entityDisplaySettings.shouldDisplayAttribute(attribute);
    }
    
    /**
     * Notifies any listeners that the user has changed a setting within this panel
     */
    void onSettingChanged() {
        
        if(settingChangedCommand != null) {
            settingChangedCommand.execute();
        }
    }
    
    /**
     * Registers the given command as a listener that will be invoked whenever a setting is
     * changed within this panel
     * 
     * @param settingChangedCommand the command to invoke whenever a setting is changed. Can
     * be set to null in order to stop listening for when a setting is changed.
     */
    public void setSettingChangedCommand(Command settingChangedCommand) {
        this.settingChangedCommand = settingChangedCommand;
    }
    
    /**
     * Builds a renderable label for the given entity that contains only the attribute data that is
     * explictly allowed by the user's current display settings
     * 
     * @param entity the entity that a label is being built for. Cannot be null.
     * @return a renderable label containing only the entity data allowed by the user's display settings
     */
    public String buildEntityLabel(AbstractMapDataPoint entity) {
        return entityDisplaySettings.buildEntityLabel(entity);
    }
}
