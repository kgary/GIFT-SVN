/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A widget that displays an icon indicating that an element can be dragged by
 * the user
 * 
 * @author nroberts
 */
public class DragIcon extends FlowPanel {
    /** Interface to allow CSS file access */
    public interface Bundle extends ClientBundle {
        /** The instance of the bundle */
        public static final Bundle INSTANCE = GWT.create(Bundle.class);

        /**
         * The specific css resource
         * 
         * @return the css resource
         */
        @NotStrict
        @Source("TreeItemStyles.css")
        public MyResources css();
    }

    /** Interface to allow CSS style name access */
    interface MyResources extends CssResource {
        /**
         * The drag icon style
         * 
         * @return the style name for the drag icon
         */
        String dragIcon();
    }

    /** The CSS resource */
    private static final MyResources CSS = Bundle.INSTANCE.css();

    static {
        /* Make sure the css style names are accessible */
        Bundle.INSTANCE.css().ensureInjected();
    }

    /**
     * Creates a new drag icon
     */
    public DragIcon() {
        super();

        addStyleName(CSS.dragIcon());

        add(new Icon(IconType.ELLIPSIS_V));
        add(new Icon(IconType.ELLIPSIS_V));
    }
}
