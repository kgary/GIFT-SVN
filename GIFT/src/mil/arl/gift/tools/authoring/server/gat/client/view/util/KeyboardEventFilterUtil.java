/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import java.util.Arrays;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyEvent;

/**
 * A class used to filter out key events for GWT input elements.
 * 
 * @author nroberts
 */
public class KeyboardEventFilterUtil {
	
	/** Key codes representing keys used when editing values. */
	private final static int[] EDITING_KEY_CODES = {
		KeyCodes.KEY_DELETE,
		KeyCodes.KEY_BACKSPACE,
		KeyCodes.KEY_ENTER,
		KeyCodes.KEY_INSERT
	};
	
	/** Key codes representing keys used in navigation. */
	private final static int[] NAVIGATION_KEY_CODES = {
		KeyCodes.KEY_TAB,
		KeyCodes.KEY_LEFT,
		KeyCodes.KEY_RIGHT,
		KeyCodes.KEY_UP,
		KeyCodes.KEY_DOWN,
		KeyCodes.KEY_HOME,
		KeyCodes.KEY_END,
		KeyCodes.KEY_PAGEUP,
		KeyCodes.KEY_PAGEDOWN
	};
	
	/** Key codes representing keys whose corresponding characters are used in numeric values. */
	private final static int[] NUMERIC_KEY_CODES = {
		KeyCodes.KEY_ZERO,
		KeyCodes.KEY_ONE,
		KeyCodes.KEY_TWO,
		KeyCodes.KEY_THREE,
		KeyCodes.KEY_FOUR,
		KeyCodes.KEY_FIVE,
		KeyCodes.KEY_SIX,
		KeyCodes.KEY_SEVEN,
		KeyCodes.KEY_EIGHT,
		KeyCodes.KEY_NINE,
		KeyCodes.KEY_NUM_ZERO,
		KeyCodes.KEY_NUM_ONE,
		KeyCodes.KEY_NUM_TWO,
		KeyCodes.KEY_NUM_THREE,
		KeyCodes.KEY_NUM_FOUR,
		KeyCodes.KEY_NUM_FIVE,
		KeyCodes.KEY_NUM_SIX,
		KeyCodes.KEY_NUM_SEVEN,
		KeyCodes.KEY_NUM_EIGHT,
		KeyCodes.KEY_NUM_NINE,
		KeyCodes.KEY_NUM_PERIOD,
		KeyCodes.KEY_NUM_MINUS,
		190 // Key code for '.' on a non-numeric keyboard.
	};
	
	/** A filter recognizing all key codes of characters that can be used in number values. */
	private final static int[][] NUMBER_VALUE_FILTER = {
		EDITING_KEY_CODES,
		NAVIGATION_KEY_CODES,
		NUMERIC_KEY_CODES
	};

	static{
		
		// Sort the key codes to speed up searching. If the codes change, this will ensure they are in order.
		Arrays.sort(EDITING_KEY_CODES);
		Arrays.sort(NAVIGATION_KEY_CODES);
		Arrays.sort(NUMERIC_KEY_CODES);
	}
	
	/**
	 * Prevents the browser's default action for the specified key event if the key represented by the event does not 
	 * represent a numeric character, is not used in navigation, and is not used for special editing purposes. This 
	 * method can be used to create number-only text boxes by adding a key press handler to pass in the key events.
	 * 
	 * @param event The event to be filtered
	 * @return True, if the key is filtered out and the browser's default action is prevented. False, otherwise.
	 */
	public static boolean filterOutNonNumericKeys(NativeEvent event){
		
		if(event.getType().equals(BrowserEvents.KEYDOWN) 
				|| event.getType().equals(BrowserEvents.KEYUP) 
				|| event.getType().equals(BrowserEvents.KEYPRESS)){					
					
					int keyCode = event.getKeyCode();	
					
					if(event.getShiftKey() 											//ignore alternate (shift) keys
							|| isFilteredOut(keyCode, NUMBER_VALUE_FILTER)){		//ignore key codes that are filtered out
						
						event.preventDefault();
						return true;
					}
			}
		
		return false;	
	}
	
	/**
	 * Prevents the browser's default action for the specified key event if the key represented by the event does not 
	 * represent a numeric character, is not used in navigation, and is not used for special editing purposes. This 
	 * method can be used to create number-only text boxes by adding a key press handler to pass in the key events.
	 * 
	 * @param event The event to be filtered
	 * @return True, if the key is filtered out and the browser's default action is prevented. False, otherwise.
	 */
	public static boolean filterOutNonNumericKeys(KeyEvent<?> event){
		
		return filterOutNonNumericKeys(event.getNativeEvent());
	}
	
	/**
	 * Returns whether or not the given key code is filtered out by the given filter. 
	 * 
	 * Filtering works by checking a collection of accepted key codes to see if the given key code is present.
	 * 
	 * @param keyCode The key code to be filtered
	 * @param filter The filter with which to filter the key code
	 * @return True, if the key code is not accepted by the filter and is filtered out. False, otherwise.
	 */
	private static boolean isFilteredOut(int keyCode, int[][] filter){
		
		for(int[] acceptedKeyCodeArray : filter){
			if(Arrays.binarySearch(acceptedKeyCodeArray, keyCode) >= 0){
				
				//the key code is accepted
				return false;
			}
		}
		
		//the key code is not accepted
		return true;
	}
}
