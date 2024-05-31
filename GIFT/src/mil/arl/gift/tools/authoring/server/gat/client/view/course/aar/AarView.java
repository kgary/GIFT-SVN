/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.aar;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * The Interface AarView.
 */
public interface AarView extends IsWidget, IsSerializable{
	
	/**
	 * The Interface Presenter.
	 */
	public interface Presenter{

		/**
		 * Start.
		 */
		void start();
		
		/**
		 * Stop.
		 */
		void stop();
	}
    
   
   /**
    * Gets the full screen input.
    *
    * @return the full screen input
    */
   HasValue<Boolean> getFullScreenInput();
   
   /**
    * Gets the full screen input.
    *
    * @return the full screen input
    */
   HasEnabled getFullScreenInputHasEnabled();
   
   /**
    * Gets the disabled input.
    *
    * @return the disabled input
    */
   HasValue<Boolean> getDisabledInput();
   
   /**
    * Gets the disabled input.
    *
    * @return the disabled input
    */
   HasEnabled getDisabledInputHasEnabled();
}
