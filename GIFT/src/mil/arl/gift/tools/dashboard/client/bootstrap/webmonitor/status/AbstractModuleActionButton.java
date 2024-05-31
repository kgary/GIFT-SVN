/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.status;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Trigger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.ModuleTypeEnum;

/**
 * A button used to perform an action targeting one or more GIFT modules
 * 
 * @author nroberts
 */
public abstract class AbstractModuleActionButton extends Composite implements HasText{

    private static AbstractModuleActionButtonUiBinder uiBinder = GWT.create(AbstractModuleActionButtonUiBinder.class);

    interface AbstractModuleActionButtonUiBinder extends UiBinder<Widget, AbstractModuleActionButton> {
    }
    
    /** The tooltip used to indicate why the button is disabled */
    @UiField
    protected Tooltip tooltip;
    
    /** 
     * The actual button that is rendered. This button is wrapped by this widget to provide
     * additional functionality, such as showing tooltips when disabled
     */
    @UiField
    protected Button button;
    
    /** The module(s) that this button interacts with when its command is invoked */
    protected List<ModuleTypeEnum> modules;

    /** The reason to display for disabling this button */
    private String disableReason;

    /**
     * Creates a new button that invokes an action on the given module(s)
     * 
     * @param modules one or more modules to invoke an action on. Cannot be null or empty.
     */
    public AbstractModuleActionButton(List<ModuleTypeEnum> modules) {
        
        if(modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException("The list of modules to launch cannot be null or empty.");
        }
        
        this.modules = modules;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        /* Invoke the module(s) action on click */
        this.button.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                onModuleAction(event);
            }
        });
        
        setEnabled(true);
    }

    /**
     * Gets the module(s) that this button invokes its action on
     * 
     * @return the module(s) to interact with. Will not be null or empty.
     */
    public List<ModuleTypeEnum> getModules() {
        return modules;
    }
    
    /**
     * Sets the reason that should be displayed for disabling this button
     * 
     * @param reason the reason for disabling. Can be null.
     */
    public void setDisableReason(String reason) {
        
        this.disableReason = reason;
        
        redrawTooltip();
    }
    
    /**
     * Redraws the tooltip to reflect the current enabled state of the button
     */
    private void redrawTooltip() {
        
        if(button.isEnabled()) {
            tooltip.hide();
            tooltip.setTitle(null);
            
        } else {
            tooltip.setTrigger(Trigger.HOVER);
            tooltip.setTitle(disableReason);
        }
    }

    /**
     * Sets whether this button should be enabled
     * 
     * @param enabled whether the button should be enabled.
     */
    public void setEnabled(boolean enabled) {
        
        button.setEnabled(enabled);
        
        redrawTooltip();
    }
    
    /**
     * Invokes an action on the target module(s) when the button is
     * interacted with
     * 
     * @param event the event that triggered the action. Will not be null.
     */
    public abstract void onModuleAction(ClickEvent event);
    
    @Override
    public String getText() {
        return button.getText();
    }
    
    @Override
    public void setText(String text) {
        button.setText(text);
    }
}
