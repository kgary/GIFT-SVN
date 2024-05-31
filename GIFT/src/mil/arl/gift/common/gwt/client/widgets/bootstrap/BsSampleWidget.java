/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;


import org.gwtbootstrap3.client.ui.Container;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;



/**
 * The Bootstrap Sample Widget can be used to stub out new screen functionality in the BsSampleWidget.ui.xml file without
 * having to add code.  It provides an easy way to layout a screen without having to add logic to the widget.  It is intended
 * that the BsSampleWidget is used as a 'playground' to test the layout & style of various bootstrap components.
 * @author nblomberg
 *
 */
public class BsSampleWidget extends AbstractBsWidget {

    private static BootstrapSampleWidgetUiBinder uiBinder = GWT.create(BootstrapSampleWidgetUiBinder.class);

    // This field isn't used in code, but is only here to show that we're using bootstrap 3 components and
    // to make sure that the build can find the reference to the bootstrap3 classes (like Container).
    @UiField
    Container sampleContainer;
    
    interface BootstrapSampleWidgetUiBinder extends UiBinder<Widget, BsSampleWidget> {
    }

    /**
     * Constructor
     */
    public BsSampleWidget() {
    	 
        initWidget(uiBinder.createAndBindUi(this));
        
    }
    
    

    
}
