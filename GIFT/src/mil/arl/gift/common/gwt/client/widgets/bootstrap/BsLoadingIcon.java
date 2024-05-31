/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;


import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;



/**
 * The BsLoadingIcon class is a simple widget that encapsulates the "loading" state of an icon widget.
 * It provides accessors to easily start loading and stop loading of the icon which handle the required
 * functionality to change the icon from a loading state to a non loading state internally.
 * 
 * It is meant that other UI panels can reference this BsLoadingIcon class in the UIBinder (ui.xml file)
 * so that this component can be reused in multiple places.  For an example of where this is done,
 * See the BsLoginWidget.ui.xml and BsLoginWidget.java files.
 * 
 * @author nblomberg
 *
 */
public class BsLoadingIcon extends AbstractBsWidget {

    private static BootstrapLoadingIconUiBinder uiBinder = GWT.create(BootstrapLoadingIconUiBinder.class);

    // This field isn't used in code, but is only here to show that we're using bootstrap 3 components and
    // to make sure that the build can find the reference to the bootstrap3 classes (like Container).
    @UiField
    Icon ctrlLoadIcon;
    
    interface BootstrapLoadingIconUiBinder extends UiBinder<Widget, BsLoadingIcon> {
    }

    /**
     * Constructor
     */
    public BsLoadingIcon() {
    	 
        initWidget(uiBinder.createAndBindUi(this));
                
        // Hide by default.
        this.setVisible(false);
    }
    
    /**
     * Set the icon type. This uses the fontawesome icon template for
     * vector based icons.  The icons available are located in
     * org.gwtbootstrap3.client.ui.constants.IconType
     * @param type - The icon type (GEAR, BOOK, etc)
     */
    public void setType(IconType type) {
        ctrlLoadIcon.setType(type);
    }
    
    /**
     * Sets the icon size.  The sizes available are loacted in
     * org.gwtbootstrap3.client.ui.constants.IconSize
     * @param size - The size of the icon (LARGE, TIMES2, etc)
     */
    public void setSize(IconSize size) {
        ctrlLoadIcon.setSize(size);
    }
    
    
    /**
     * Sets the icon to a loading 'state'.  This means
     * the icon will be made visible and it will be made to
     * spin.
     */
    public void startLoading() {
       
        ctrlLoadIcon.setSpin(true);
        setVisible(true);
    }
    
    /**
     * Sets the icon to stop loading state.  This means
     * the icon will stop spinning and will be made hidden.
     */
    public void stopLoading() {
        ctrlLoadIcon.setSpin(false);
        setVisible(false);
    }
    
    /**
     * Return whether the loading icon is visible because the {@link #startLoading()} method was
     * called and the {@link #stopLoading()} has NOT been called yet.
     * @return the result of {@link #isVisible()}
     */
    public boolean isLoading(){
        return isVisible();
    }
    
}
