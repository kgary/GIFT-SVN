/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

/**
 * An {@link UIObject} that acts as a simple wrapper around an {@link Element} so that it
 * can be used with the Bootstrap Animate library.
 * <br/><br/>
 * Wrapping basic DOM elements using this class is unfortunately necessary when calling the 
 * library's animation functions, since they only take in UIObjects as arguments even though
 * the underlying JavaScript logic only needs an element. This was likely an
 * oversight in the way the libary's animation method signatures were defined.
 * 
 * @author nroberts
 */
public class AnimateWrapper extends UIObject{

    /**
     * Creates a new wrapper that wraps the given element in a UIObject
     * 
     * @param element the element to wrap. Cannot be null.
     */
    public AnimateWrapper(Element element) {
        
        if(element == null) {
            throw new IllegalArgumentException("The element to construct an animation wrapper for cannot be null.");
        }
        
        setElement(element);
    }
}
