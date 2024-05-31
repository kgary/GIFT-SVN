/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HasValue;

import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamObjectTreeItem.PickMode;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * A widget wrapping a {@link TeamPicker} that only allows the author to pick a single team member from the 
 * scenario's team organization. Unlike {@link TeamPicker}, teams cannot be picked using this widget, since 
 * picking a team could potentially entail picking multiple team members within it.
 * 
 * @author nroberts
 */
public class TeamMemberPicker extends ScenarioValidationComposite implements HasValue<String>{
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TeamMemberPicker.class.getName());
    
    /**
     * The team picker that is wrapped by this widget. Normally, it would make more sense to simply extend
     * {@link TeamPicker} for a case like this, but doing so in this case would mean allowing multiple values
     * since TeamPicker implements HasValue&lt;List&lt;String&gt;&gt;.
     * <br/><br/>
     * In order to allow this widget to enforce using only a single string value, it can't extend TeamPicker
     * directly. As an alternative, this widget instead wraps TeamPicker as a composite in order to reuse all 
     * of the same UI components and existing functionality while publicly exposing only a single string value.
     */
    private final EditableTeamPicker picker;
    
    /**
     * Creates a new widget wrapping a {@link TeamPicker} that only allows the author to pick a single team 
     * member from the scenario's team organization.
     */
    public TeamMemberPicker() {
        this(false);
    }
    
    /**
     * Creates a new widget wrapping a {@link TeamPicker} that only allows the author to pick a single team 
     * member from the scenario's team organization and and optionally selects a team member
     * for the author automatically if none is explicitly selected.
     * 
     * @param mustPickTeamMember  mustSelectTeamMember whether this picker should automatically select a 
     * team member if the author doesn't explicitly select one
     */
    public TeamMemberPicker(boolean mustPickTeamMember) {
        this(mustPickTeamMember, false);
    }

    /**
     * Creates a new widget wrapping a {@link TeamPicker} that only allows the author to pick a single team 
     * member from the scenario's team organization and and optionally selects a team member
     * for the author automatically if none is explicitly selected.
     * 
     * @param mustPickTeamMember whether this picker should automatically select a 
     * team member if the author doesn't explicitly select one
     * @param mustUseEntityMarker whether the team members picked by this picker must use entity markers to
     * identify themselves in the training application. If true, validation errors will be shown if the author
     * picks a team member that does not use a entity marker.
     */
    public TeamMemberPicker(boolean mustPickTeamMember, boolean mustUseEntityMarker) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("TeamMemberPicker(");
            List<Object> params = Arrays.<Object>asList(mustPickTeamMember, mustUseEntityMarker);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        picker = new EditableTeamPicker(mustPickTeamMember, mustUseEntityMarker, PickMode.SINGLE);

        initWidget(picker);
        
        picker.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                
                String value = null;
                if(picker.getValue() != null && !picker.getValue().isEmpty()) {
                    
                    //if the team picker's value is not empty, use the first string in its value as this widget's value
                    value = picker.getValue().get(0);
                }
                
                ValueChangeEvent.fire(TeamMemberPicker.this, value);
            }
        });
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        //Nothing to validate. Validation is handled through the wrapped team picker.
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        //Nothing to validate. Validation is handled through the wrapped team picker.
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(picker);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public String getValue() {
        
        if(picker.getValue() != null && !picker.getValue().isEmpty()) {
            
            //if the team picker's value is not empty, return the first string in its value as this widget's value
            return picker.getValue().get(0);
        }
        
        return null;
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        
        List<String> listValue = new ArrayList<>();
        listValue.add(value);
        
        picker.setValue(listValue, fireEvents);
    }
    
    /**
     * Sets the label text for this picker
     *
     * @param text the plain text to use as the label
     */
    public void setLabel(String text) {
        picker.setLabel(text);
    }

    /**
     * Sets the label text for this picker and renders it as HTML
     *
     * @param html the HTML to use as the label
     */
    public void setLabel(SafeHtml html) {
        picker.setLabel(html);
    }
    
    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        picker.setReadonly(isReadonly);
    }
}
