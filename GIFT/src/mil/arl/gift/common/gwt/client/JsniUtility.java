/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.storage.client.Storage;

import mil.arl.gift.common.gwt.shared.ServerPropertiesWrapper;
import mil.arl.gift.common.gwt.shared.ServerProperties;

/**
 * Provides useful JSNI functions to handle some web based functionality such as
 * printing
 * 
 * @author wpearigen
 *
 */
public class JsniUtility {
    
    /** The key used to save server properties to the session's local storage */
	private static final String SERVER_PROPERTIES_KEY = "serverProperties";
	
    /*
	 * server properties contains the settings for the Piwik Configuration
	 */
	private static ServerProperties serverProperties;

	/**
	 * Sets the server properties to share throughout this client. These server
	 * properties will also be saved to the local session storage, where they
	 * can be retrieved by child iframes without a server call if they are part of
	 * the same server-side session.
	 * 
	 * @param properties the properies to save. Can be null.
	 */
	public static void setServerProperties(ServerProperties properties) {
        
        serverProperties = properties;
        
        // save the server properties to the local session storage so that iframes using
        // the same session don't have to re-request them from the server
        Storage storage = Storage.getSessionStorageIfSupported();
        if(properties != null && storage != null) {
            storage.setItem(SERVER_PROPERTIES_KEY, JsonUtils.stringify(ServerPropertiesWrapper.wrap(properties)));
        }
	}

	/**
     * Gets the server properties shared throughout this client. If no server properties
     * have been saved by this client yet, then this method will also check the local session
     * storage to see if another client with the same server-side session (i.e. an iframe) 
     * has already shared server properties.
     * 
     * @param properties the properties to save. Can be null.
     */
	public static ServerProperties getServerProperties() {
	    
	    if(serverProperties != null) {
	        return serverProperties;
	    }
	    
	    // if no properties are available, see if any have been saved to the local session storage
	    Storage storage = Storage.getSessionStorageIfSupported();
        if(storage != null && serverProperties == null) {
            
            String jsonProps = storage.getItem(SERVER_PROPERTIES_KEY);
            
            if(jsonProps != null) {
                ServerPropertiesWrapper nativeProps = JsonUtils.safeEval(jsonProps);
                serverProperties = nativeProps.unwrap();
            }
        }
        
        return serverProperties;
	}

	/**
	 * Prints out individual or multiple html elements, regardless of other
	 * present elements. Format the html in the string parameter and changes
	 * will be present in the print
	 * 
	 * 
	 * @param objectHtml
	 *            string of all the HTML of the object to be printed. In case of
	 *            some windows, must concatenate all inner html together instead
	 *            of the html of the whole object
	 * @return true
	 */
	public static native boolean printhtml(String objectHtml) /*-{
		var mywindow = window.open('', '', 'height=400,width=600');
		mywindow.document.write(objectHtml);

		mywindow.document.close(); // necessary for IE >= 10
		mywindow.focus(); // necessary for IE >= 10

		mywindow.print();
		mywindow.close();

		return true;
	}-*/;

	/**
	 * Gets whether the window this function is invoked from is the topmost
	 * frame of the web page or not. This can be used to determine if the
	 * current window is within an iframe or not.
	 * 
	 * @return whether the current window is the topmost frame of the webpage
	 */
	public static native boolean isTopmostFrame()/*-{

		try {

			//determine if this window is the topmost frame of the webpage
			return $wnd.self == $wnd.top;

		} catch (e) {

			//if an exception occurs while checking $wnd.top, then a Cross-Origin security exception was thrown,
			//which means this window MUST be in an iframe, since the topmost window is on a
			//different domain.
			return false;
		}
	}-*/;
	
	/**
	 * Copies the given element's text to the current operating system's
	 * clipboard so that it can be pasted by the user. Note that this only
	 * copies characters that users can manually select in their browsers, so it
	 * won't include things like formatting, HTML tags, or invisible characters.
	 * 
	 * @param elem
	 *            the element whose text should be copied
	 * @return whether or not the copy was successful. This should generally
	 *         always return unless this method is invoked in a browser that
	 *         does not support document.execCommand() on the 'copy' command.
	 *         All major browsers support this command, so the return value
	 *         exists mainly to catch edge cases.
	 */
	public native static boolean copyTextToClipboard(Element elem)/*-{

		//Nick: This code is based on a solution found on StackOverflow at
		//http://stackoverflow.com/questions/22581345/click-button-copy-to-clipboard-using-jquery/22581382#22581382

		// create hidden text element, if it doesn't already exist
		var targetId = "_hiddenCopyText_";
		var isInput = elem.tagName === "INPUT" || elem.tagName === "TEXTAREA";
		var origSelectionStart, origSelectionEnd;
		
		if (isInput) {
			
			// can just use the original source element for the selection and copy
			target = elem;
			origSelectionStart = elem.selectionStart;
			origSelectionEnd = elem.selectionEnd;
			
		} else {
			
			// must use a temporary form element for the selection and copy
			target = document.getElementById(targetId);
			
			if (!target) {
				var target = document.createElement("textarea");
				target.style.position = "absolute";
				target.style.left = "-9999px";
				target.style.top = "0";
				target.id = targetId;
				document.body.appendChild(target);
			}
			
			target.textContent = elem.textContent;
		}
		// select the content
		var currentFocus = document.activeElement;
		target.focus();
		target.setSelectionRange(0, target.value.length);

		// copy the selection
		var succeed;
		try {
			succeed = document.execCommand("copy");
		} catch (e) {
			succeed = false;
		}
		
		// restore original focus
		if (currentFocus && typeof currentFocus.focus === "function") {
			currentFocus.focus();
		}

		if (isInput) {
			// restore prior selection
			elem.setSelectionRange(origSelectionStart, origSelectionEnd);
		} else {
			// clear temporary content
			target.textContent = "";
		}
		
		return succeed;

	}-*/;

	/**
	 * Configures the Piwik Tracker for the user session. This call invokes the
	 * JSNI setup tracker method with the analytics information configured in
	 * the config.
	 */
	public static void setTracker() {
		if (getServerProperties().isClientAnalyticsEnabled()) {
			setupTracker(getServerProperties().getClientAnalyticsUrl());
		}
	}

	/**
	 * JSNI function which loads and the Piwik library script, and configures
	 * the tracking variables on the page. This call invokes the JSNI setup
	 * tracker method with the analytics information configured in the config.
	 * This code snippet is provided by PIWIK, and intended to be used verbatim.
	 */
	private static native void setupTracker(String trackingServerIp) /*-{
	try {
    	$wnd.top._paq = $wnd.top._paq || [];
    	$wnd.top._paq.push(["setDomains", ["*.cloud.gifttutoring.org"]]);
    	//_paq.push(['trackPageView']);
    	//Custom
    	var afterHash = location.hash.slice(1);
    	if (typeof afterHash == "undefined" || afterHash.length < 1){
    	$wnd.top._paq.push(['setDocumentTitle', afterHash]);
    	}
    	//End Custom
    	$wnd.top._paq.push(['trackPageView']);
    	$wnd.top._paq.push(['enableLinkTracking']);
    	$wnd.top._paq.push(['enableHeartBeatTimer']);
    	$wnd.top._paq.push(['trackAllContentImpressions']);
    	(function() {
    	$wnd.top._paq.push(['setTrackerUrl',trackingServerIp+'piwik.php']);
    	$wnd.top._paq.push(['setSiteId', '1']);
    	var d=$doc;
    	var g=d.createElement('script');
    	var s=d.getElementsByTagName('script')[0];
    	g.type='text/javascript';
    	g.async=true; g.defer=true;
    	g.src=trackingServerIp+'piwik.js'; 
    	s.parentNode.insertBefore(g,s);
    	})();
    	}  catch(err) {
    	console.log(err);
    	}
	
	}-*/;

	/**
	 * Emit a Piwik Event with the provided title
	 * 
	 * @param event
	 *            title of the event being emitted
	 */
	public static void trackEvent(String event) {
		emitEvent(event);
	}

	/**
	 * JSNI function to emit a Piwik Event with the provided title
	 * 
	 * @param event
	 *            title of the event being emitted
	 */
	private static native void emitEvent(String eventName) /*-{
    	try {
    	if( $wnd.top._paq != null ){
    		$wnd.top._paq.push(['trackEvent', 'Event','Clicked',eventName ]);
    	}
    	
    	}  catch(err) {
    	console.log(err);
    	}
	}-*/;

	/**
	 * Emit a Piwik Page Change Event with the provided page and triggering
	 * event
	 * 
	 * @param event
	 *            title of the event causing the page change
	 * @param newPage
	 *            title of the page being opened
	 */
	public static void changePage(String event, String newPage) {
		emitPageChange(event, newPage);
	}

	/**
	 * JSNI Function to Emit a Piwik Page Change Event with the provided page
	 * and triggering event
	 * 
	 * @param event
	 *            title of the event causing the page change
	 * @param newPage
	 *            title of the page being opened
	 */
	private static native void emitPageChange(String eventName, String newPage) /*-{
        if( $wnd.top._paq != null ) {
            $wnd.top._paq.push(['trackEvent', 'Navigation','PageChange',eventName ]);
            $wnd.top._paq.push(['setDocumentTitle', newPage]);
            $wnd.top._paq.push(['trackPageView']);
            $wnd.top._paq.push(['trackAllContentImpressions']);
        }
    }-*/;
	
	/**
	 * Deselects all text currently selected by the browser (i.e. text the user has selected on the page by dragging the cursor)
	 */
	public static native void clearBrowserSelection()/*-{
		
		if ($doc.selection) {
			
			//special case for some older browsers
	        $doc.selection.empty();
	        
	    } else if ($wnd.getSelection) {
	    	
	    	//W3C standard in use by all modern browsers
	    	$wnd.getSelection().removeAllRanges();
	    }
	}-*/;

	/**
	* Gets the name of the used browser.
	*/
	public static native String getBrowserName() /*-{
	    return navigator.userAgent.toLowerCase();
	}-*/;
	
	/**
	* Returns true if the current browser is Chrome.
	*/
	public static boolean isChromeBrowser() {
	    return getBrowserName().contains("chrome");
	}

	/**
	* Returns true if the current browser is Firefox.
	*/
	public static boolean isFirefoxBrowser() {
	    return getBrowserName().contains("firefox");
	}

	/**
	* Returns true if the current browser is IE (Internet Explorer).
	*/
	public static boolean isIEBrowser() {
	    return getBrowserName().contains("msie") || // for IE < 11
	            getBrowserName().contains("rv:11.0");  //for IE 11
	}
	
	/**
	 * Requests that the browser displays the given element in full screen mode, provided that the browser supports it. Note that
	 * this is only a basic implementation that does not keep track of the Promise object that is returned according to the HTML5
	 * specification.
	 * <br/><br/>
	 * For more details about the HTML5 Fullscreen API, see 
	 * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Fullscreen_API">here</a>.
	 * 
	 * @param element
	 */
    public static native void requestFullscreen(Element element)/*-{
        
		if (element.requestFullscreen) {
			element.requestFullscreen();
			
		} else if (element.mozRequestFullScreen) {
			element.mozRequestFullScreen();
			
		} else if (element.webkitRequestFullscreen) {
			element.webkitRequestFullscreen();
			
		} else if (element.msRequestFullscreen) {
			element.msRequestFullscreen();
		}
    }-*/;
    
    /**
     * Requests that the browser exits full screen mode, provided that the browser supports it. Note that
     * this is only a basic implementation that does not keep track of the Promise object that is returned according to the HTML5
     * specification.
     * <br/><br/>
     * For more details about the HTML5 Fullscreen API, see 
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Fullscreen_API">here</a>.
     * 
     * @param element
     */
    public static native void exitFullscreen()/*-{
        
        if(@mil.arl.gift.common.gwt.client.JsniUtility::getFullscreenElement()){
            
            if ($doc.exitFullscreen) {
                $doc.exitFullscreen();
                
            } else if ($doc.webkitExitFullscreen) {
                $doc.webkitExitFullscreen();
                
            } else if ($doc.mozCancelFullScreen) {
                $doc.mozCancelFullScreen();
                
            } else if ($doc.msExitFullscreen) {
                $doc.msExitFullscreen();
            }
        }
    }-*/;
    
    /**
     * Gets the element that is currently being shown in full screen mode, or null if no element is being shown in full screen mode.
     * 
     * @return the element currently being shown in full screen mode, or null such element exists
     */
    public static native Element getFullscreenElement()/*-{
        
        var fullscreenElement = $doc.fullscreenElement;
        
        if(!fullscreenElement || fullscreenElement == null){
            fullscreenElement = $doc.webkitFullscreenElement;
        }
        
        if(!fullscreenElement || fullscreenElement == null){
            fullscreenElement = $doc.mozFullScreenElement;
        }
        
        if(!fullscreenElement || fullscreenElement == null){
            fullscreenElement = $doc.msFullscreenElement;
        }
        
        return fullscreenElement;
    }-*/;
    
    /**
     * Gets the first node in the document that match the given CSS selector string,
     * or null, if no such node exists.
     * This underlying implementation of this method uses JSNI to call
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelector">
     * Document.querySelector()
     * </a>.
     * 
     * @param selectors a string containing one or more CSS selectors. If null or an invalid
     * CSS selector string, a JavaScriptException will be thrown.
     * @return the first node matching the given CSS selectors
     */
    public static native Node querySelector(String selectors)/*-{
        return $doc.querySelector(selectors);
    }-*/;
    
    /**
     * Gets all of the nodes in the document that match the given CSS selector string,
     * or null, if no such nodes exist.
     * This underlying implementation of this method uses JSNI to call
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelectorAll">
     * Document.querySelectorAll()
     * </a>.
     * 
     * @param selectors a string containing one or more CSS selectors. If null or an invalid
     * CSS selector string, a JavaScriptException will be thrown.
     * @return all of the nodes matching the given CSS selectors
     */
    public static native NodeList<Node> querySelectorAll(String selectors)/*-{
        return $doc.querySelectorAll(selectors);
    }-*/;
    
    /**
     * Gets the first node within the given element that match the given CSS selector string,
     * or null, if no such node exists.
     * This underlying implementation of this method uses JSNI to call
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/querySelector">
     * Element.querySelector()
     * </a>.
     * 
     * @param selectors a string containing one or more CSS selectors. If null or an invalid
     * CSS selector string, a JavaScriptException will be thrown.
     * @return the first node matching the given CSS selectors
     */
    public static native Node querySelector(Node element, String selectors)/*-{
        return element.querySelector(selectors);
    }-*/;
    
    /**
     * Gets all of the nodes within the given element that match the given CSS selector string,
     * or null, if no such nodes exist.
     * This underlying implementation of this method uses JSNI to call
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelectorAll">
     * Document.querySelectorAll()
     * </a>.
     * 
     * @param selectors a string containing one or more CSS selectors. If null or an invalid
     * CSS selector string, a JavaScriptException will be thrown.
     * @return all of the nodes matching the given CSS selectors
     */
    public static native NodeList<Node> querySelectorAll(Node element, String selectors)/*-{
        return element.querySelectorAll(selectors);
    }-*/;

    /**
     * Loads and plays the audio file at the given URL until it has completed.
     * 
     * @param url the URL of the audio file to play. If null, no audio will be played.
     * @return the audio element used to play the audio file. Can be used to control how the audio is
     * played. Will be null if the given URL is null.
     */
    public static AudioElement playAudio(String url){
        return playAudio(url, null);
    }
    
    /**
     * Loads and plays the audio file at the given URL until it has completed.
     * 
     * @param url the URL of the audio file to play. If null, no audio will be played.
     * @return the audio element used to play the audio file. Can be used to control how the audio is
     * played. Will be null if the given URL is null.
     * @param volume the volume to play the audio at. If null, the default volume will be used.
     */
    public static native AudioElement playAudio(String url, Double volume)/*-{
        var audio = null;
        if(url != null){
            audio = new Audio(url);
            if(volume != null){
                audio.volume = volume;
            }
            audio.play();
        }
        
        return audio;
    }-*/;

    /**
     * Checks if the most recent navigation performed on the page was a reload. This can be used
     * to distinguish between whether a tab was reloaded or whether it was duplicated from another
     * browser tab with the same session data.
     * 
     * @return whether this browser tab was reloaded.
     */
    public static native boolean wasPageReloaded()/*-{
       return ($wnd.performance.navigation && $wnd.performance.navigation.type === 1) ||
            $wnd.performance
              .getEntriesByType('navigation')
              .map(function(nav){
                  return nav.type
              })
              .includes('reload');
    }-*/;
}
