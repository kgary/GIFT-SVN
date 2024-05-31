/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.MIMEType;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.RecordingCallback;
import mil.arl.gift.tools.dashboard.client.MediaInputUtil.UploadCallback;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;

/**
 * A wrapper widget in which other focusable (i.e. input-accepting) widgets can be placed in order to allow
 * users to record media instead of interacting with said widgets.
 * 
 * @author nroberts
 */
public class RecordingBooth extends Composite implements IsWidget, HasWidgets{
    
    /** The URL host value corresponding to the local host of this client */
    private static final String LOCAL_HOST = "localhost";

    private static RecordingBoothUiBinder uiBinder = GWT.create(RecordingBoothUiBinder.class);

    interface RecordingBoothUiBinder extends UiBinder<Widget, RecordingBooth> {
    }
    
    /** Whether the user has input the keyboard command to record audio from the microphone (i.e. Ctrl + Shift)*/
    protected boolean hasCommandedRecord;
    
    /** The tooltip shown by the button used to start/stop recording */
    @UiField
    protected Tooltip recordTooltip;
    
    /** The button used to start/stop recording*/
    @UiField
    protected Button recordButton;
    
    /** The panel containing the widget in the recording booth */
    @UiField
    protected SimplePanel wrapper;
    
    /** A deck used to switch between the widget in the recording booth and any media that is recorded */
    @UiField
    protected DeckPanel deck;
    
    /** The panel displaying the media that has been recorded, if applicable */
    @UiField(provided=true)
    protected AudioPlayer audioPlayer = new AudioPlayer() {
        
        @Override
        public void onUrlChanged(String newUrl) {
            
            if(deck == null) {
                return; //UI isn't ready yet
            }
            
            //show the audio player if it has the URL for a recording or hide it otherwise
            if(StringUtils.isNotBlank(newUrl)) {
                deck.showWidget(deck.getWidgetIndex(audioPlayer));
                
            } else {
                deck.showWidget(deck.getWidgetIndex(wrapper));
            }
        }
    };
    
    /** The raw data recorded from the user by this recording booth*/
    private Blob recordedBlob = null;
    
    /**
     * Creates a new recording booth with no widget inside it. This recording booth will essentially act as an
     * non-interactive, empty container element until a widget is placed inside it
     */
    public RecordingBooth() {
        initWidget(uiBinder.createAndBindUi(this));
        
        recordButton.addMouseDownHandler(new MouseDownHandler() {
            
            @Override
            public void onMouseDown(MouseDownEvent event) {
                
                //avoid blurring the widget in the recording booth so it retains focus
                event.preventDefault();
                event.stopPropagation();
                
                //toggle recording on/off
                setRecording(!hasCommandedRecord);
            }
        });
        
        audioPlayer.setUrl(null);
    }
    
    /**
     * Creates a new recording booth with the given widget inside it
     * 
     * @param widget the widget to place in the recording booth. Can be null.
     */
    public RecordingBooth(Widget widget) {
        this();
        setWrappedWidget(widget);
    }
    
    /** 
     * Removes the widget placed inside this recording booth
     */
    @Override
    public void clear() {
        wrapper.clear();
    }
    
    /**
     * Gets the widget inside this recording booth. Can return null, if no widget is inside this recording booth.
     */
    @Override
    public Widget getWidget() {
        return wrapper.getWidget();
    }
    
    /** {@inheritDoc} */
    @Override
    public Iterator<Widget> iterator() {
        // Simple iterator for the widget
        return new Iterator<Widget>() {

            boolean hasElement = getWidget() != null;

            Widget returned = null;

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return hasElement;
            }

            /** {@inheritDoc} */
            @Override
            public Widget next() {
                if (!hasElement || (getWidget() == null)) {
                    throw new NoSuchElementException();
                }
                hasElement = false;
                return (returned = getWidget());
            }

            /** {@inheritDoc} */
            @Override
            public void remove() {
                if (returned != null) {
                    RecordingBooth.this.remove(returned);
                }
            }
        };
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean remove(final Widget w) {
        // Validate.
        if (getWidget() != w) {
            return false;
        }
        // Logical detach.
        clear();
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Widget child) {
        if (getWidget() != null) {
            throw new IllegalStateException("Can only contain one child widget");
        }
        setWrappedWidget(child);
    }
    
    /**
     * Sets the widget that should be placed in this recording booth. If the user focuses on this
     * widget, then this recording booth will allow them to record media as an alternative to 
     * interacting with the widget.
     * 
     * @param w the widget to place in the recording booth. Can be null.
     */
    public void setWidget(final IsWidget w) {
        setWrappedWidget(w.asWidget());
    }
    
    /**
     * Sets the widget that should be placed in this recording booth. If the user focuses on this
     * widget, then this recording booth will allow them to record media as an alternative to 
     * interacting with the widget.
     * 
     * @param w the widget to place in the recording booth. Can be null.
     */
    /*
     * Note: Can't name this method setWidget since Composite has it flagged as deprecated, which causes build
     * warnings when compiled. This also annoyingly prevents us from using the AcceptsOneWidget interface, so callers
     * simply have to know that this widget can only wrap one other widget.
     */
    public void setWrappedWidget(final Widget w) {
        // Validate
        if (w == getWidget()) {
            return;
        }
        
        if(w != null && !(w instanceof FocusWidget)) {
            throw new IllegalArgumentException("A recording booth can only wrap widgets that can be focused on");
        }

        // Detach new child
        if (w != null) {
            w.removeFromParent();
        }

        // Remove old child
        if (getWidget() != null) {
            remove(getWidget());
        }

        // Logical attach, but don't physical attach; done by jquery.
        wrapper.setWidget(w);
        if (w == null) {
            return;
        }
        
        FocusWidget widget = (FocusWidget) w;
        
        //start/stop recording audio from the user's mic when they press Ctrl + Shift
        widget.addDomHandler(new KeyUpHandler() {
            
            @Override
            public void onKeyUp(KeyUpEvent event) {
                
                if((event.getNativeEvent().getShiftKey() || event.getNativeEvent().getCtrlKey())
                        && hasCommandedRecord) {
                    
                    setRecording(false);
                }
            }
            
        }, KeyUpEvent.getType());
        widget.addDomHandler(new KeyDownHandler() {
            
            @Override
            public void onKeyDown(KeyDownEvent event) {
                
                if(event.getNativeEvent().getCtrlKey() 
                        && event.getNativeEvent().getShiftKey()
                        && !MediaInputUtil.isRecording()
                        && !hasCommandedRecord) {
                    
                    setRecording(true);
                }
            }
            
        }, KeyDownEvent.getType());
        
        //stop recording and hide the record button if the user stops focusing on the widget in the recording booth
        widget.addBlurHandler(new BlurHandler() {
            
            @Override
            public void onBlur(BlurEvent event) {
                
                if(MediaInputUtil.isRecording()) {
                    MediaInputUtil.cancelRecording();
                    setRecording(false);
                }
            }
        });

    }
    
    /**
     * Sets whether or not this recording booth should record media from the user
     * 
     * @param recording whether to record media
     */
    public void setRecording(boolean recording) {
        
        if(recording) {
            
            if(!MediaInputUtil.hasMediaDevices()) {
                
                if(LOCAL_HOST.equals(Window.Location.getHostName())){
                    
                    //handle when browser does not support recording
                    UiManager.getInstance().displayErrorDialog(
                            "Recording Not Supported by Browser", 
                            "GIFT was unable to begin recording from your connected media devices because "
                            + "your browser does not support such recordings."
                            + "<br/><br/>Please use a different browser if you want to record media for GIFT.", 
                            null);
                
                } else if(DeploymentModeEnum.SERVER == UiManager.getInstance().getDeploymentMode()) {
                    
                    //handle when GIFT is in server mode on an insecure host that is blocked by security
                    UiManager.getInstance().displayDetailedErrorDialog(
                            "Recording Not Allowed by Browser", 
                            new DetailedExceptionSerializedWrapper(new DetailedException(
                                "Due to your browser's security restrictions, GIFT was unable to find any connected media devices to "
                                + "record from because your browser is connected to GIFT's webpages using an insecure (i.e. http://) host."
                                + "<br/><br/>Please contact the network administrator for the GIFT website you are using and tell"
                                + "them that GIFT cannot record from you because the server is not enforcing a secure connection.", 
                                Window.Location.getProtocol() + "//" + Window.Location.getHost() 
                                + " is not recognized as a secure host by this browser.", 
                                null
                     )));
                    
                } else {
                    
                    //handle when GIFT is in desktop/simple mode on an insecure host that is blocked by security
                    String localHostUrl = Window.Location.getHref().replace(Window.Location.getHostName(), LOCAL_HOST);
                    
                    UiManager.getInstance().displayDetailedErrorDialog(
                            "Recording Not Allowed by Browser", 
                            new DetailedExceptionSerializedWrapper(new DetailedException(
                                "Due to your browser's security restrictions, GIFT was unable to find any connected media devices to "
                                + "record from because your browser is connected to GIFT's webpages using an insecure (i.e. http://) host."
                                + "<br/><br/>If you are running GIFT locally on the same machine that you are accessing this webpage from, "
                                + "try connecting to 'localhost' instead your machine's IP address using <a href='" 
                                + localHostUrl + "'>this link</a>.", 
                                Window.Location.getProtocol()+ "//" +  Window.Location.getHost() 
                                + " is not recognized as a secure host by this browser.", 
                                null
                    )));
                }
                
                return;
            }
            
            hasCommandedRecord = true;
            
            Notify.notify("GIFT is recording audio");
            recordButton.setIcon(IconType.MICROPHONE);
            recordButton.setType(ButtonType.DANGER);
            recordTooltip.setTitle("Click to stop recording (or release Ctrl + Shift)");
            
            recordedBlob = null;
            
            MediaInputUtil.startRecording(new RecordingCallback() {
                
                @Override
                public void onFinishedRecording(Blob recordingBlob) {
                    
                    //adjust the UI to indicate that media has been recorded
                    recordedBlob = recordingBlob;
                    recordTooltip.hide();
                    audioPlayer.setUrl(recordingBlob.getLocalUrl());
                }

                @Override
                public void onRecordingFailed(String message) {
                    UiManager.getInstance().displayDetailedErrorDialog("Recording Failed", 
                        new DetailedExceptionSerializedWrapper(new DetailedException(
                            "GIFT was unable to record from your browser due to an unexpected error.", 
                            message, 
                            null
                    )));
                }
            });
            
        } else {
            
            hasCommandedRecord = false;
            
            Notify.notify("GIFT has stopped recording audio");
            recordButton.setIcon(IconType.MICROPHONE_SLASH);
            recordButton.setType(ButtonType.PRIMARY);
            recordTooltip.setTitle("Click to start recording (or hold Ctrl + Shift)");
            
            MediaInputUtil.stopRecording();
        }
    }
    
    /**
     * Deletes any recorded media currently being shown by this recording booth and resets the
     * booth to its initial state
     */
    public void resetRecording() {
        recordedBlob = null;
        audioPlayer.setUrl(null);
    }
    
    /**
     * Gets whether this recording booth has any recorded media stored within it
     * 
     * @return whether this booth has any recorded media
     */
    public boolean hasRecording() {
        return recordedBlob != null;
    }
    
    /**
     * Saves the media recorded by this recording booth to disk
     * 
     * @param params an optional set of parameters to pass to the recorder service. Can be null. Affects how the recording is saved.
     * @param callback the callback to invoke once the media is saved or once the save operation fails. Cannot be null.
     */
    public void saveRecording(RecorderParams params, final AsyncCallback<String> callback) {
        
        if(recordedBlob != null) {
            MediaInputUtil.uploadRecording(params, MIMEType.WAV, recordedBlob, new UploadCallback() {
                
                @Override
                public void onUploaded(String fileRef) {
                    callback.onSuccess(fileRef);
                }

                @Override
                public void onUploadFailed(String message) {
                    callback.onFailure(new Exception(message));
                }
            });
        }
    }
    
    /**
     * Loads a URL for an existing recording into this booth so that it can be displayed,
     * deleted, or played back for the user
     * 
     * @param url the URL of the existing recording to load. Can be null, in which case, nothing
     * will be loaded by the audio player.
     */
    public void setExtistingRecordingUrl(String url) {
        audioPlayer.setUrl(url);
    }
}
