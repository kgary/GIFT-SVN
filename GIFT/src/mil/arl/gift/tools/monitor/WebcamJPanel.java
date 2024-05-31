/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The webcam panel contains one or more webcams that can be viewed on the monitor.  This class contains logic
 * to update the images for each viewed webcam. 
 * 
 * @author mhoffman
 */
public class WebcamJPanel extends JPanel {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(WebcamJPanel.class);
    
    private static final long serialVersionUID = 1L;
        
    /** contains the current camera (when not selecting view all cameras) */
    private CameraDetails currentCamera = null;
    
    /** contains the list of ip cameras available */
    private static final String CAMERA_LIST_FILE = PackageUtil.getConfiguration() + File.separator + "tools" +File.separator+ "monitor" +File.separator+ "cameras.txt";
    
    /** location of the image to display when no webcam image was found */
    private final URL IMAGE_NOT_AVAILABLE_URL = getClass().getResource("images/no_pic_available.png");
    
    /** location to place captured images */
    private static final String DEFAULT_CAPTURE_LOCATION = PackageUtil.getToolLog();
    
    /** list model to add cameras too for camera list */
    private DefaultListModel<CameraDetails> cameraListModel = new DefaultListModel<>();

    /** the image to display when no webcam image was found */
    private Image noImageImage = null;

    /** 
     * Class constructor - creates new form WebcamJPanel.  Reads in the cameras configuration file.
     */
    public WebcamJPanel() {
        initComponents();
        
        cameraList.setModel(cameraListModel);
        
        readCamerasList();
        
        //force the first camera in the list to be selected and shown
        if(!cameraListModel.isEmpty()){
            cameraList.setSelectedIndex(0);
        }
        
        try {
            noImageImage = ImageIO.read(IMAGE_NOT_AVAILABLE_URL);
        } catch (IOException e) {
            logger.error("Unable to find image at "+IMAGE_NOT_AVAILABLE_URL, e);
        }

        captureImagePathLabel.setText("(to: "+DEFAULT_CAPTURE_LOCATION+")");
    }
    
    /**
     * Read the camera list input file and populate the list of cameras for selection
     */
    private void readCamerasList(){
        
        Properties properties = new Properties();
        
        try {
            logger.info("Reading camera list from file named "+CAMERA_LIST_FILE);
            
            //load a properties file
            properties.load(new FileInputStream(CAMERA_LIST_FILE));
            
            logger.info("Finished reading camera list file and found "+properties.size()+" cameras");
            
            List<CameraDetails> cameras = new ArrayList<>();
            for(Object key : properties.keySet()){                
                CameraDetails details = new CameraDetails((String)key, properties.getProperty((String)key));
                cameras.add(details);
            }
            
            //sort and add
            Collections.sort(cameras);
            for(CameraDetails details : cameras){
                cameraListModel.addElement(details);
            }
             
        } catch (IOException ex) {
            logger.error("Caught exception while trying to read camera list file named "+CAMERA_LIST_FILE, ex);
        }
    }
    
    /**
     * Handles what to do and show if a webcam image was not found.
     * 
     * @param outputCanvas - the canvas responsible for displaying the image that wasn't found
     * @param url - the url at which an image is being looked for at
     * @param showDialog - whether a dialog should be presented informing the user that the webcam wasn't found
     */
    private void cameraNotFound(WebcamImageCanvas outputCanvas, String url, boolean showDialog){
        
        if(showDialog && this.isVisible()){
            JOptionPane.showConfirmDialog(this, "Unable to find camera at "+url, "Camera Not Found", JOptionPane.DEFAULT_OPTION);
        }
        
        if(noImageImage != null){
            outputCanvas.setWebcamImage(noImageImage);
        }
    }
    
    /**
     * Container for a camera's details and maintains the update image timer task.
     * 
     * @author mhoffman
     *
     */
    private class CameraDetails implements Comparable<CameraDetails>{
        
        /** the port to use for webcams */
        private static final int PORT = 8081;
        
        /** the display name of the camera */
        private String name;
        
        /** the ip address of the camera */
        private String ipaddr;
        
        /** the generated url of the camera using the ip address and port */
        private String url;
        
        /** the update webcam image timer task used to get new images to draw */
        private WebcamUpdateTimerTask updateTimerTask = null;
        
        /**
         * Class constructor - set attributes
         * 
         * @param name - the display name for the camera
         * @param ipaddr - the IP address of the camera
         */
        public CameraDetails(String name, String ipaddr){
            this.name = name;
            this.ipaddr = ipaddr;
            url = "http://"+getIPAddr()+":"+PORT;
        }
        
        /**
         * Return the display name of the camera
         * 
         * @return String
         */
        public String getName(){
            return name;
        }
        
        /**
         * Return the ip address of the camera 
         * 
         * @return String
         */
        public final String getIPAddr(){
            return ipaddr;
        }
        
        /**
         * Start the update image timer
         * 
         * @param canvas - where the camera images will be drawn
         */
        public void startTimer(WebcamImageCanvas canvas){
            
            if(updateTimerTask != null){
                cancelTimer();
            }
            
            //get FPS
            int fps = MonitorModuleProperties.getInstance().getMaxWebcamFPS();
            if(fps <= 0){
                logger.error("Unable to monitor webcam because the Maximum webcam FPS property value is an invalid value of "+fps);
            }else{
            
                updateTimerTask = new WebcamUpdateTimerTask(canvas, name, url);
                Timer webcamUpdateTimer = new Timer(name + "- CameraTimer");
                webcamUpdateTimer.scheduleAtFixedRate(updateTimerTask, 0, (long)((1/(double)fps)*1000));
            }
        }
        
        /**
         * Cancel the update webcam image timer
         */
        public void cancelTimer(){
            
            if(updateTimerTask != null){
                updateTimerTask.cancel();
            }
        }        

        @Override
        public int compareTo(CameraDetails other) {
            return this.name.compareTo(other.getName());
        }
        
        @Override
        public String toString(){
            return name;
        }

    }

    /**
     * For rendering the stream of a webcam
     */
    private class WebcamImageCanvas extends Canvas {

        private static final long serialVersionUID = 1L;

        private Image webcamImage = null;

        /**
         * Sets the current image in the stream
         * 
         * @param webcamImage - an image from the camera
         */
        public void setWebcamImage(Image webcamImage) {
            this.webcamImage = webcamImage;
            this.repaint();
        }
        
        /**
         * Create a buffered image of the current webcam image that can be used to write to a file
         * 
         * @return BufferedImage
         */
        private BufferedImage getImage() {
            int w = webcamImage.getWidth(null);
            int h = webcamImage.getHeight(null);
            int type = BufferedImage.TYPE_INT_RGB;  
            BufferedImage dest = new BufferedImage(w, h, type);
            Graphics g = dest.createGraphics();
            g.drawImage(webcamImage,0,0,null);
            g.dispose();
            return dest;
        }

        @Override
        public void update(Graphics g) {
            paint(g);
        }

        @Override
        public void paint(Graphics g) {
            if (webcamImage != null) {
                int newWidth = this.getWidth();
                int newHeight = this.getHeight();
                double thumbRatio = (double) this.getWidth() / (double) this.getHeight();
                int imageWidth = webcamImage.getWidth(null);
                int imageHeight = webcamImage.getHeight(null);
                double aspectRatio = (double) imageWidth / (double) imageHeight;

                if (thumbRatio < aspectRatio) {
                    newHeight = (int) (newWidth / aspectRatio);
                } else {
                    newWidth = (int) (newHeight * aspectRatio);
                }

                int xOffset = (this.getWidth() - newWidth) / 2;
                int yOffset = (this.getHeight() - newHeight) / 2;

                g.drawImage(webcamImage, xOffset, yOffset, newWidth, newHeight, this);
            }
        }
    }

    /**
     * For maintaining the connection to the webcam stream
     */
    private class WebcamUpdateTimerTask extends TimerTask {

        /** the canvas used to draw the webcam image */
        private final WebcamImageCanvas outputCanvas;

        /** the url of the camera to get images from */
        private final String baseUrl;
        
        /** the display name of the camera */
        private final String name;

        /** the url to http get from */
        private String webcamImageUrl = null;
        
        /** whether the timeout hint has been displayed to the user since the last successful image was captured */
        private boolean showTimeoutHint = true;

        /**
         * Constructor - set attributes
         * 
         * @param outputCanvas Where to output the stream to
         * @param name - the display name of the camera
         * @param baseUrl The URL of the stream
         */
        public WebcamUpdateTimerTask(WebcamImageCanvas outputCanvas, String name, String baseUrl) {
            this.outputCanvas = outputCanvas;
            this.baseUrl = baseUrl;
            this.name = name;
        }

        /**
         * Gets a new ID from the stream
         */
        private void getNewId() {
            webcamImageUrl = null;
            try {
                double randomId = new Random().nextDouble();
                URL url = new URL(baseUrl + "/get?id=" + randomId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                int c;
                while ((c = in.read()) != -1) {
                    byteArrayOut.write(c);
                }

                String replyString = new String(byteArrayOut.toByteArray());
                if (replyString.equals("ok")) {
                    webcamImageUrl = baseUrl + "/out.jpg?id=" + randomId;
                    logger.info("Got ID " + randomId + " for the webcam named "+name+" at " + baseUrl);
                }
                
                //reset in case there is a timeout
                showTimeoutHint = true;

            } catch (MalformedURLException ex) {
                logger.debug("Caught an exception while getting a new id for the webcam named "+name, ex);
            } catch (@SuppressWarnings("unused") SocketTimeoutException e) {

                cameraNotFound(outputCanvas, baseUrl, showTimeoutHint);
                showTimeoutHint = false;
                
            } catch (IOException ex) {
                logger.debug("Caught an exception while trying to retrieve image for webcam named "+name, ex);
                
                cameraNotFound(outputCanvas, baseUrl, showTimeoutHint);
                showTimeoutHint = false;
            }
        }

        /**
         * Gets the image from the stream
         */
        private void getWebcamImage() {
            try {
                if (webcamImageUrl != null) {
                    URL url = new URL(webcamImageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(1000);
                    BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                    ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                    int c;
                    while ((c = in.read()) != -1) {
                        byteArrayOut.write(c);
                    }

                    Image image = Toolkit.getDefaultToolkit().createImage(
                            byteArrayOut.toByteArray());

                    outputCanvas.setWebcamImage(image);
                }

            } catch (MalformedURLException ex) {
                logger.debug("Caught an exception while getting an image from a webcam named "+name, ex);
            } catch (@SuppressWarnings("unused") SocketTimeoutException e) {
            } catch (IOException ex) {
                logger.debug("Caught an exception while getting an image from a webcam named "+name, ex);
            }
        }

        @Override
        public void run() {
            if (webcamImageUrl != null) {
                getWebcamImage();
            } else {
                getNewId();
            }
        }
    }
    
    /**
     * Used to display all webcams at once
     */
    private void watchAllWebcams(){

        for(int i = 0; i < cameraListModel.size(); i++){
            CameraDetails details = cameraListModel.get(i);
            
            if (webcamImagePanel.getComponentCount() == 1) {
                //only have at most 2 rows of images
                ((java.awt.GridLayout) webcamImagePanel.getLayout()).setRows(2);
            }
            watchWebcam(details);
        }
        
        logger.info("Watching "+cameraListModel.size()+" cameras at once");
    }

    /**
     * Adds a webcam stream to the panel
     * 
     * @param url The URL of the webcam stream
     */
    private void watchWebcam(CameraDetails details) {
        
        WebcamImageCanvas webcamImageCanvas = new WebcamImageCanvas(); 
        webcamImagePanel.add(webcamImageCanvas);
        details.startTimer(webcamImageCanvas);
        
        logger.info("Watching webcam "+details);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        webcamImagePanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cameraList = new javax.swing.JList<CameraDetails>();
        captureButton = new javax.swing.JButton();
        displayAllCheckbox = new java.awt.Checkbox();
        captureImagePathLabel = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(930, 580));
        setMinimumSize(new java.awt.Dimension(930, 580));
        setPreferredSize(new java.awt.Dimension(930, 580));

        webcamImagePanel.setLayout(new java.awt.GridLayout(1, 0, 5, 5));

        cameraList.setBorder(javax.swing.BorderFactory.createTitledBorder("Cameras"));
        cameraList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        cameraList.setToolTipText("<html>The list of cameras found<br> in cameras.txt");
        cameraList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                cameraListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(cameraList);

        captureButton.setText("Capture Image");
        captureButton.setToolTipText("<html>Capture the current image on <br>the selected camera and save <br>it to a file.");
        captureButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureButtonActionPerformed(evt);
            }
        });

        displayAllCheckbox.setLabel("Display All");
        displayAllCheckbox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                displayAllCheckboxItemStateChanged(evt);
            }
        });

        captureImagePathLabel.setText("path");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(displayAllCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(captureImagePathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .addComponent(captureButton, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 369, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(displayAllCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(captureButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(captureImagePathLabel)
                .addContainerGap(83, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(webcamImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 728, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(webcamImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for when a camera is selected from the list of cameras.  It will cause the selected camera
     * images to be displayed.
     * 
     * @param evt
     */
    private void cameraListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_cameraListValueChanged
        
        CameraDetails details = cameraList.getSelectedValue();
        if(details != null && details != currentCamera){
            
            if(currentCamera != null && currentCamera != details){
                currentCamera.cancelTimer();
            }
            
            webcamImagePanel.removeAll();
            
            watchWebcam(details);
            webcamImagePanel.revalidate();
            
            currentCamera = details;
        }
        
    }//GEN-LAST:event_cameraListValueChanged

    /**
     * Event handler for when the capture image button is selected.  This will save the currently selected webcam image to disk.
     * 
     * @param evt
     */
    private void captureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_captureButtonActionPerformed

        //save current image to file
        BufferedImage image = ((WebcamImageCanvas)webcamImagePanel.getComponent(0)).getImage();
            
        try {
            CameraDetails details = cameraList.getSelectedValue();
            if(details != null){
                String filename = DEFAULT_CAPTURE_LOCATION + "/"+ details.getName() + "_" + TimeUtil.formatCurrentTime() + ".png";
                ImageIO.write(image, "png", new File(filename));
                logger.info("Wrote webcam image for "+details+" to "+filename);
            }else{
                JOptionPane.showConfirmDialog(this, "Unable to capture image because a camera has not been selected", "Failed to Capture Image", JOptionPane.DEFAULT_OPTION);
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showConfirmDialog(this, "Unable to capture image", "Failed to Capture Image", JOptionPane.DEFAULT_OPTION);
        } 

        
    }//GEN-LAST:event_captureButtonActionPerformed

    /**
     * Event handler for the "display all" checkbox.  If the checkbox is checked it will display all the webcams.  If it is unchecked
     * only the last selected webcam will be displayed.
     * 
     * @param evt
     */
    private void displayAllCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_displayAllCheckboxItemStateChanged
        
        if(displayAllCheckbox.getState()){
            //display all cameras
            
            logger.info("Changing webcam display to display all cameras");
            
            captureButton.setEnabled(false);
            cameraList.setEnabled(false);
            
            //remove all panels
            webcamImagePanel.removeAll();
            ((java.awt.GridLayout) webcamImagePanel.getLayout()).setRows(1);
            
            watchAllWebcams();
            webcamImagePanel.revalidate();

        }else{
            //display the last selected, if available, camera
            
            logger.info("Changing webcam display to display a single camera");
            
            captureButton.setEnabled(true);
            cameraList.setEnabled(true);
            
            //stop timers
            for(int i = 0; i < cameraListModel.size(); i++){
                CameraDetails details = cameraListModel.get(i);
                details.cancelTimer();
            }
            
            //remove all panels
            webcamImagePanel.removeAll();
            ((java.awt.GridLayout) webcamImagePanel.getLayout()).setRows(1);
            
            if(cameraList.getSelectedValue() != null){
                
                watchWebcam(currentCamera);
                webcamImagePanel.revalidate();
            }
            
        }
    }//GEN-LAST:event_displayAllCheckboxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<CameraDetails> cameraList;
    private javax.swing.JButton captureButton;
    private javax.swing.JLabel captureImagePathLabel;
    private java.awt.Checkbox displayAllCheckbox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel webcamImagePanel;
    // End of variables declaration//GEN-END:variables
}
