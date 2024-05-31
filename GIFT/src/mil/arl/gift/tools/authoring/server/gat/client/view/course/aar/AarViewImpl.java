/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.aar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public class AarViewImpl extends Composite implements AarView {

    private static AarViewImplUiBinder uiBinder = GWT
            .create(AarViewImplUiBinder.class);

    interface AarViewImplUiBinder extends UiBinder<Widget, AarViewImpl> {
    } 
    
    @UiField
    protected CheckBox fullScreen;
    
    /** The disable checkbox. */
    @UiField
    protected CheckBox disabled;

    public AarViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
    }

    @Override
    public HasValue<Boolean> getFullScreenInput() {
        return fullScreen;
    }
    
    @Override
    public HasEnabled getFullScreenInputHasEnabled(){
        return fullScreen;
    }

    @Override
    public HasValue<Boolean> getDisabledInput(){
        return disabled;
    }
    
    @Override
    public HasEnabled getDisabledInputHasEnabled(){
        return disabled;
    }
}
