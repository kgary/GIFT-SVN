/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.tools;

import javax.swing.JFrame;

import mil.arl.gift.common.io.ImageUtil;

/**
 * This class contains the common GUI layout for sensor controller panels.
 * 
 * @author jleonard
 */
public class SensorControllerJPanel extends javax.swing.JPanel {
    
	private static final long serialVersionUID = 1L;

	/** the title for all */
    private static final String TITLE = "Sensor Controller";

    private SensorController controller = null;

    private double rangeDifference = 0, minimumValue = 0;

    public SensorControllerJPanel(SensorController ctrlr, double minimumSensorValue, double maximumSensorValue) {
        initComponents();
        this.controller = ctrlr;
        sensorValueProgressBar.setMinimum(0);
        sensorValueProgressBar.setMaximum(100);
        rangeDifference = maximumSensorValue - minimumSensorValue;
        minimumValue = minimumSensorValue;
        Thread updateThread = new Thread("SensorControllerUpdate"){
            @Override
            public void run() {
                while(controller.isAlive()) {
                    int val = (int)(((controller.getSensorValue()-minimumValue)/(rangeDifference))*100.0);
                    sensorValueProgressBar.setValue(val + (int)minimumValue);
                    try {
                        sleep(1);
                    } catch (@SuppressWarnings("unused") InterruptedException ex) {
                    }
                }
                
                dying();
            }
        };
        updateThread.start();
    }
    
    /**
     * The sensor controller is no longer alive, therefore this jpanel needs to close
     */
    private void dying(){
        ((JFrame)this.getTopLevelAncestor()).dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        sensorValueProgressBar = new javax.swing.JProgressBar();
        increaseButton = new javax.swing.JButton();
        steadyButton = new javax.swing.JButton();
        decreaseButton = new javax.swing.JButton();
        sensorNameLabel = new javax.swing.JLabel();

        sensorValueProgressBar.setOrientation(1);
        sensorValueProgressBar.setString("0");

        increaseButton.setText("Increase");
        increaseButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                increaseButtonActionPerformed(evt);
            }
        });

        steadyButton.setText("Steady");
        steadyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                steadyButtonActionPerformed(evt);
            }
        });

        decreaseButton.setText("Decrease");
        decreaseButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decreaseButtonActionPerformed(evt);
            }
        });

        sensorNameLabel.setText("placeholder");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sensorNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(steadyButton, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                            .addComponent(increaseButton, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                            .addComponent(decreaseButton, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(sensorValueProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sensorNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(sensorValueProgressBar, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(increaseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(steadyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decreaseButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void steadyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_steadyButtonActionPerformed
        controller.steadyRate();
    }//GEN-LAST:event_steadyButtonActionPerformed

    private void increaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_increaseButtonActionPerformed
        controller.increaseRate();
    }//GEN-LAST:event_increaseButtonActionPerformed

    private void decreaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decreaseButtonActionPerformed
        controller.decreaseRate();
    }//GEN-LAST:event_decreaseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton decreaseButton;
    private javax.swing.JButton increaseButton;
    protected javax.swing.JLabel sensorNameLabel;
    private javax.swing.JProgressBar sensorValueProgressBar;
    private javax.swing.JButton steadyButton;
    // End of variables declaration//GEN-END:variables

    public static SensorController showPanel(String sensorName, SensorControllerInterface controllerInterface, double minimumSensorValue, double maximumSensorValue) {
        JFrame frame = new JFrame(TITLE);
        frame.setIconImage(ImageUtil.getInstance().getSystemIcon());        
        SensorController controller = new SensorController(controllerInterface);
        SensorControllerJPanel panel = new SensorControllerJPanel(controller, minimumSensorValue, maximumSensorValue);
        panel.sensorNameLabel.setText(sensorName);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        //don't allow the user to close the sensor controller dialog manually (i.e. by selecting the "x" on the window)
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        return controller;
    }
}
