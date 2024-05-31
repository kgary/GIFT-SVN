/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;

import com.google.gwt.core.client.JavaScriptObject;

import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.Blob;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;

/**
 * A utility class used to interact with a client's connected input devices (e.g. microphone) and 
 * record media (i.e. audio) from said devices.
 * <br/><br/>
 * Note that accessing input devices <i>always</i> requires permission from the user that can be
 * denied at any time for any reason, so any uses of this class should have fallback logic in place should
 * permission be denied.
 * 
 * @author nroberts
 */
public class MediaInputUtil {

    /** 
     * The media recorder used to write media data from the input stream 
     * to a format that can be stored and transmitted 
     */
    private static JavaScriptObject recorder = null;
    
    /** The stream of data from the input devices requested from the browser during recording */
    private static JavaScriptObject inputStream = null;
    
    /** Whether recording has been cancelled by the user */
    private static boolean recordingCancelled = false;
    
    /** A callback to invoke when recording is finished or when an error occurs during recording */
    private static RecordingCallback recordingCallback = null;
    
    /** A callback to invoke when a recording is uploaded or when an error occurs during the upload */
    private static UploadCallback uploadCallback = null;
    
    /** A callback to invoke when a recording is deleted or when an error occurs during the delete */
    private static DeleteCallback deleteCallback = null;

    /**
     * The MIME type that can be used to specify the uploaded file content type.
     * 
     * @author sharrison
     */
    public enum MIMEType {
        /** Recorded .wav content type */
        WAV("audio/wav"),
        /** .mp4 content type */
        MP4("video/mp4");

        /** The MIME type */
        private final String type;

        /**
         * Constructor
         * 
         * @param type the MIME type
         */
        private MIMEType(String type) {
            this.type = type;
        }
    }

    /**
     * Gets whether or not media is currently being recorded from the client
     * 
     * @return whether media is being recorded
     */
    public static native boolean isRecording()/*-{
        
         var recorder = @mil.arl.gift.tools.dashboard.client.MediaInputUtil::recorder;
         return recorder && recorder.state == "recording";
    }-*/;
    
    /**
     * Cancels (i.e. stops) recording media input (i.e. audio) from the client's connected input devices 
     * (e.g. microphone) and,  unlike {@link #stopRecording()}, does NOT return the recorded data. If 
     * media input is not being recorded, then this method will do nothing.
     * <br/><br/>
     * Unlike {@link #stopRecording()}, this method does not require permission from the user to 
     * perform its logic.
     */
    public static void cancelRecording() {
        recordingCancelled = true;
        stopRecording();
    }

    /**
     * Stops recording media input (i.e. audio) from the client's connected input devices (e.g. microphone).
     * If media input is not being recorded, then this method will do nothing.
     * <br/><br/>
     * Unlike {@link #stopRecording()}, this method does not require permission from the user to 
     * perform its logic.
     */
    public static native void stopRecording()/*-{
        
        if(!@mil.arl.gift.tools.dashboard.client.MediaInputUtil::isRecording()){
            return; //already not recording, so return
        }
        
        var recorder = @mil.arl.gift.tools.dashboard.client.MediaInputUtil::recorder;
        var inputStream = @mil.arl.gift.tools.dashboard.client.MediaInputUtil::inputStream;
        
        //stop streaming audio from the user's microphone
        if(recorder){
            recorder.stop();
            inputStream.getAudioTracks()[0].stop();
        }
    }-*/;
    
    /**
     * Begins recording media input (i.e. audio) from the client's connected input devices (e.g. microphone). 
     * If media input is already being recorded, then this method will do nothing.
     * If the user has not yet given permission to record media input, they will be prompted to allow it.
     * <br/><br/>
     * If the user denies permission or permission is blocked by browser security, then no media will be recorded
     * from the client.
     * 
     * @param callback the callback to invoke when recording is finished or when an error occurs 
     * during recording. Cannot be null.
     */
    public static void startRecording(final RecordingCallback callback) {
        
        if(callback == null) {
            throw new IllegalArgumentException("The callback used to handle the recording cannot be null");
        }
        
        if(isRecording()) {
            
            /* Do not allow multiple recordings at once. This generally isn't supported by browsers,
             * so if a second recording is started, stop the previous one and start a new one. */
            final RecordingCallback previousCallback = recordingCallback;
            recordingCallback = new RecordingCallback() {
                
                @Override
                public void onRecordingFailed(String message) {
                    previousCallback.onRecordingFailed(message);
                    
                    startRecording(callback); //start the new recording
                }
                
                @Override
                public void onFinishedRecording(Blob recordingBlob) {
                    previousCallback.onFinishedRecording(recordingBlob);
                    
                    startRecording(callback); //start the new recording
                }
            };
            
            stopRecording(); //stop the old recording
            return;
        }
        
        MediaInputUtil.recordingCallback = callback;
        
        startRecording();
    }
    
    /**
     * Begins recording media input (i.e. audio) from the client's connected input devices (e.g. microphone). 
     * If media input is already being recorded, then this method will do nothing.
     * If the user has not yet given permission to record media input, they will be prompted to allow it.
     * <br/><br/>
     * If the user denies permission or permission is blocked by browser security, then no media will be recorded
     * from the client.
     */
    private static native void startRecording() /*-{
        
        if(@mil.arl.gift.tools.dashboard.client.MediaInputUtil::isRecording()()){
            return; //already recording, so return
        }
        
        @mil.arl.gift.tools.dashboard.client.MediaInputUtil::recordingCancelled = false;
        
        //handle when permission to a device's input stream is granted
        var onInputStreamGranted = function(stream){
        
            try{
                var inputStream = stream;
                @mil.arl.gift.tools.dashboard.client.MediaInputUtil::inputStream = inputStream;
                
                //record the requested input stream to byte data
                var options = {
                    audioBitsPerSecond : 16000,
                    mimeType: 'audio/webm'
                };
                var recordedChunks = [];
                var recorder = new MediaRecorder(stream, options);
                @mil.arl.gift.tools.dashboard.client.MediaInputUtil::recorder = recorder;
            
                //as bytes come in from the input stream during recording, write them down
                recorder.addEventListener('dataavailable', function(e) {
                    if (e.data.size > 0) {
                        recordedChunks.push(e.data);
                    }
                });
            
                //when recording is stopped, output the written data to a format that can be stored and transmitted
                recorder.addEventListener('stop', function() {
                    
                    try{
                        if(@mil.arl.gift.tools.dashboard.client.MediaInputUtil::recordingCancelled){
                            return; //recording was cancelled, so do not proceed
                        }
                        
                        //save the bytes to a .wav format blob and return said blob to the caller
                        var blob = new Blob(recordedChunks, {'type' : 'audio/wav; codecs=MS_PCM'});
                        
                        @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onFinishedRecording(Lmil/arl/gift/tools/dashboard/client/bootstrap/gamemaster/Blob;)(blob);
                    
                    } catch (error){
                        @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingFailed(Ljava/lang/String;)(error.message);
                    }
                });
            
                //begin recording bytes from the media input stream
                recorder.start();
                
            } catch (error){
                @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingFailed(Ljava/lang/String;)(error.message);
            }
        };
        
        try{
            // NOTE: Need to use $wnd.navigator here instead of just navigator, since using the latter
            // gets the navigator of the GWT iframe, which will always be denied mic permissions due to
            // security restrictions on iframes (particularly in Chrome). $wnd.navigator gets the 
            // navigator of the main window, which will prompt mic permissions properly to the user
            $wnd.navigator.mediaDevices.getUserMedia({audio: true, video: false})
                .then(onInputStreamGranted)
                ["catch"](function(err){ //can't use regular .catch method call since GWT compiler throws an error
                    @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingFailed(Ljava/lang/String;)(err.message);
                });
                
        } catch (error){
            @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingFailed(Ljava/lang/String;)(error.message);
        }
    }-*/;
    
    /**
     * Uploads the given recording data to the recording servlet
     * 
     * @param params optional parameters that can be passed to the recorder servlet. Can be null.
     * @param mimeType the MIME type of the content of the blob. If null, no upload will be invoked.
     * @param blob the data to upload. If null, no upload will be invoked.
     * @param callback the callback to invoke when the upload finishes or fails
     */
    public static void uploadRecording(RecorderParams params, MIMEType mimeType, Blob blob, UploadCallback callback) {
        
        if(mimeType == null || blob == null) {
            return;
        }
        
        if(callback == null) {
            throw new IllegalArgumentException("The callback used to handle uploading cannot be null");
        }
        
        MediaInputUtil.uploadCallback = callback;
        
        uploadRecording(RecorderParams.encodeToQuery(params), mimeType.type, blob);
    }
    
    /**
     * Uploads the given recording data to the recording servlet
     * 
     * @param paramQuery an optional URL query string containing parameters to pass to the recorder servlet. Can be null.
     * @param mimeType the MIME content type of the content of the blob. If null, no upload will be invoked.
     * @param blob the data to upload. If null, no upload will be invoked.
     */
    private static native void uploadRecording(String paramQuery, String mimeType, Blob blob)/*-{
        
        try{
            //send the blob to the appropriate servlet using an HTTP POST request
            var xhr = new XMLHttpRequest();
            var query = paramQuery != null ? ('?' + paramQuery) : '';
            xhr.open('POST', '/dashboard/recorder' + query, true);
            xhr.onreadystatechange = function(){
                
                if(xhr.readyState == 4){ //DONE

                    if(xhr.status == 200){ //SUCCESS
                        @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingUploaded(Ljava/lang/String;)(xhr.responseText);
                    
                    } else { //FAILURE
                        @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingFailed(Ljava/lang/String;)(xhr.responseText);
                    }
                }
            };
            
            xhr.setRequestHeader("Content-Type", mimeType);
            xhr.send(blob);
            
        } catch (error){
            @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onUploadFailed(Ljava/lang/String;)(error.message);
        }
    }-*/;
    
    /**
     * Deletes the given recording data on the server
     * 
     * @param params contains parameters such as the path to the video file and metadata files. Can't be null.
     * @param callback the callback to invoke when the delete finishes or fails
     */
    public static void deleteRecording(RecorderParams params, DeleteCallback callback) {
        
        if(callback == null) {
            throw new IllegalArgumentException("The callback used to handle deleting cannot be null");
        }
        
        MediaInputUtil.deleteCallback = callback;
        
        deleteRecording(RecorderParams.encodeToQuery(params));
    }
    
    /**
     * Deletes the given recording data on the server
     * 
     * @param paramQuery a required URL query string containing parameters to pass to the recorder servlet. Can't be null.
     */
    private static native void deleteRecording(String paramQuery)/*-{
        
        try{
            //send the blob to the appropriate servlet using an HTTP POST request
            var xhr = new XMLHttpRequest();
            var query = paramQuery != null ? ('?' + paramQuery) : '';
            xhr.open('POST', '/dashboard/recorder' + query, true);
            xhr.onreadystatechange = function(){
                
                if(xhr.readyState == 4){ //DONE

                    if(xhr.status == 200){ //SUCCESS
                        @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingDeleted()();
                    
                    } else { //FAILURE
                        @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingDeleteFailed(Ljava/lang/String;)(xhr.responseText);
                    }
                }
            };
            
            var blob = new Blob();
            xhr.send(blob);
            
        } catch (error){
            @mil.arl.gift.tools.dashboard.client.MediaInputUtil::onRecordingDeleteFailed(Ljava/lang/String;)(error.message);
        }
    }-*/;

    /**
     * Invokes the appropriate callback when recording has finished
     * 
     * @param recordingBlob the recorded data. Cannot be null.
     */
    private static void onFinishedRecording(Blob recordingBlob) {
        
        if(recordingCallback != null) {
            recordingCallback.onFinishedRecording(recordingBlob);
        }
        
        recordingCallback = null;
    }
    
    /**
     * Invokes the appropriate callback when recording has failed
     * 
     * @param error the error message that was reported. Cannot be null.
     */
    private static void onRecordingFailed(String error) {
        
        if(recordingCallback != null) {
            recordingCallback.onRecordingFailed(error);
        }
        
        recordingCallback = null;
    }
    
    /**
     * Invokes the appropriate callback when deleting has failed
     * 
     * @param error the error message that was reported. Cannot be null.
     */
    private static void onRecordingDeleteFailed(String error) {
        
        if(deleteCallback != null) {
            deleteCallback.onDeleteFailed(error);
        }
        
        deleteCallback = null;
    }
    
    /**
     * Invokes the appropriate callback when uploading has finished
     * 
     * @param fileRef the path that the uploaded file can be accessed from. Cannot be null.
     */
    private static void onRecordingUploaded(String fileRef) {
        
        if(uploadCallback != null) {
            uploadCallback.onUploaded(fileRef);
        }
        
        uploadCallback = null;
    }
    
    /**
     * Invokes the appropriate callback when deleting has finished
     */
    private static void onRecordingDeleted() {
        
        if(deleteCallback != null) {
            deleteCallback.onDeleted();
        }
        
        deleteCallback = null;
    }
    
    /**
     * Invokes the appropriate callback when uploading has failed
     * 
     * @param error the error message that was reported. Cannot be null.
     */
    private static void onUploadFailed(String error) {
        
        if(uploadCallback != null) {
            uploadCallback.onUploadFailed(error);
        }
        
        uploadCallback = null;
    }
    
    public static native boolean hasMediaDevices() /*-{
        return $wnd.navigator.mediaDevices != null;
    }-*/;
    
    /**
     * A callback used to handle the results of recording media from the user
     * 
     * @author nroberts
     */
    public interface RecordingCallback{
        
        /**
         * Handles when recording media has finished
         * 
         * @param recordingBlob the raw media data that was recorded. Cannot be null.
         */
        public void onFinishedRecording(Blob recordingBlob);
        
        /**
         * Handles when an error occurs while recording
         * 
         * @param message the error message that was reported. Cannot be null.
         */
        public void onRecordingFailed(String message);
    }
    
    /**
     * A callback used to handle the results of uploading recorded media
     * 
     * @author nroberts
     */
    public interface UploadCallback{
        
        /**
         * Handles when a recording is uploaded
         * 
         * @param fileRef the path that the uploaded file can be accessed from. Cannot be null.
         */
        public void onUploaded(String fileRef);
        
        /**
         * Handles when an error occurs while uploading
         * 
         * @param message the error message that was reported. Cannot be null.
         */
        public void onUploadFailed(String message);
        
    }
    
    /**
     * A callback used to handle the results of deleting recorded media
     * 
     * @author mhoffman
     *
     */
    public interface DeleteCallback{
        
        /**
         * Handles when a delete operation succeeds
         */
        public void onDeleted();
        
        /**
         * Handles when an error occurs while uploading
         * 
         * @param message the error message that was reported. Cannot be null.
         */
        public void onDeleteFailed(String message);

    }
}
