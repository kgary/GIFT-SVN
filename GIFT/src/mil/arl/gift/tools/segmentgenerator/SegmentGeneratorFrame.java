/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.segmentgenerator;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.net.api.message.Message;

/**
 * A window for generating corridor segments from DIS traffic
 *
 * @author jleonard
 */
public class SegmentGeneratorFrame extends javax.swing.JFrame implements DISListener {

    private static final long serialVersionUID = 1L;

    /**
     * The delimeter to use when printing the points for the file
     */
    private static final String POINT_VALUE_DELIMITER = ",";

    /**
     * The first location chosen
     */
    private Point3d firstPoint = new Point3d();

    /**
     * The second location chosen
     */
    private Point3d secondPoint = new Point3d();

    /**
     * The third and final location chosen
     */
    private Point3d thirdPoint = new Point3d();

    private Point3d firstLinePoint = null;

    /**
     * A save dialog for choosing where to save the segments text file
     */
    final JFileChooser saveFileChooser = new JFileChooser();

    private Integer currentEntityId = null;

    private final Map<Integer, Message> entityIdToEntityState = new HashMap<>();

    private final Map<String, Integer> listOptionToEntityId = new HashMap<>();

    /**
     * Constructor
     */
    public SegmentGeneratorFrame() {
        initComponents();
        
        setIconImage(ImageUtil.getInstance().getSystemIcon());

        selectionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Integer entityId = listOptionToEntityId.get(selectionComboBox.getSelectedItem());
                if (entityId != null) {
                    currentEntityId = entityId;
                }
            }
        });

        // Create and start a thread for updating the text in the form to keep
        // it current.
        // TODO: Only the current position and distance need to be actively 
        //       updated, the rest can be done as needed
        Thread updateThread = new Thread("Segment Generator Frame - Text Update") {
            @Override
            public void run() {
                while (true) {
                    Point3d currentPoint = getCurrentPoint();
                    if (currentPoint != null) {
                        currentPositionLabel.setText(pointToString(currentPoint));

                        Point3d firstPoint = getFirstPoint();
                        firstPointLabel.setText(firstPoint.getX() + ", "
                                + firstPoint.getY() + ", "
                                + firstPoint.getZ());

                        Point3d secondPoint = getSecondPoint();
                        secondPointLabel.setText(secondPoint.getX() + ", "
                                + secondPoint.getY() + ", "
                                + secondPoint.getZ());

                        Point3d thirdPoint = getThirdPoint();
                        thirdPointLabel.setText(thirdPoint.getX() + ", "
                                + thirdPoint.getY() + ", "
                                + thirdPoint.getZ());

                        if (firstLinePoint != null) {
                            firstLinePointLabel.setText(firstLinePoint.getX() + ", "
                                    + firstLinePoint.getY() + ", "
                                    + firstLinePoint.getZ());

                            double distance = firstLinePoint.distance(currentPoint);
                            lineDistanceLabel.setText(distance + "m");
                        }
                    }
                    try {
                        sleep(500);
                    } catch (@SuppressWarnings("unused") InterruptedException ex) {
                    }
                }
            }
        };
        updateThread.start();
    }

    /**
     * Gets the last entity position sent via DIS
     *
     * @return Point3dData The last entity position
     */
    public Point3d getCurrentPoint() {
        if (currentEntityId != null) {
            Message message = entityIdToEntityState.get(currentEntityId);
            if (message != null) {
                return ((EntityState) message.getPayload()).getLocation();
            }
        }
        return null;
    }

    /**
     *
     * @return Point3dData
     */
    public Point3d getFirstPoint() {
        return firstPoint;
    }

    /**
     *
     * @return Point3dData
     */
    public Point3d getSecondPoint() {
        return secondPoint;
    }

    /**
     *
     * @return Point3dData
     */
    public Point3d getThirdPoint() {
        return thirdPoint;
    }

    /**
     * When three points have been chosen, calculates and prints the center line
     * coordinates and width in the file text area and resets the frame for
     * another
     */
    private void calculatePoints() {

        double width;
        try {
            width = Double.parseDouble(corridorWidthTextBox.getText());
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            System.out.println("Corridor width not a valid value");
            return;
        }

        Vector3d vec = new Vector3d(
                thirdPoint.getX() - secondPoint.getX(),
                thirdPoint.getY() - secondPoint.getY(),
                thirdPoint.getZ() - secondPoint.getZ());

        vec.normalize();
        vec.scale(width * 0.5);

        Point3d firstCenterPoint = new Point3d(
                firstPoint.getX() + vec.x,
                firstPoint.getY() + vec.y,
                firstPoint.getZ() + vec.z);

        Point3d secondCenterPoint = new Point3d(
                secondPoint.getX() + vec.x,
                secondPoint.getY() + vec.y,
                secondPoint.getZ() + vec.z);


        Point3d p1 = new Point3d(secondPoint.getX(), secondPoint.getY(), secondPoint.getZ());
        Point3d p2 = new Point3d(secondCenterPoint.getX(), secondCenterPoint.getY(), secondCenterPoint.getZ());
        Point3d p3 = new Point3d(thirdPoint.getX(), thirdPoint.getY(), thirdPoint.getZ());

        System.out.println(p2.distance(p1));
        System.out.println(p3.distance(p2));

        outputTextArea.setText(outputTextArea.getText()
                + "corridorWidth: " + Double.toString(width) + "\n"
                + "centerStart: " + pointToString(firstCenterPoint) + "\n"
                + "centerEnd: " + pointToString(secondCenterPoint) + "\n");

        firstPoint = new Point3d();
        secondPoint = new Point3d();
        thirdPoint = new Point3d();
    }

    /**
     * Returns the formatted string of a position point
     *
     * @param pt The point
     * @return String The formatted point string
     */
    private String pointToString(Point3d pt) {
        return pt.getX() + POINT_VALUE_DELIMITER + pt.getY()
                + POINT_VALUE_DELIMITER + pt.getZ();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectionComboBox = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        historyTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        corridorWidthTextBox = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        firstLinePointLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lineDistanceLabel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        captureLinePointButton = new javax.swing.JButton();
        captureSinglePointButton = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();

        setTitle("Segment Generator");
        setPreferredSize(new java.awt.Dimension(363, 316));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        selectionComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        getContentPane().add(selectionComboBox);

        jScrollPane3.setMaximumSize(new java.awt.Dimension(32767, 100));

        historyTextArea.setColumns(20);
        historyTextArea.setRows(5);
        jScrollPane3.setViewportView(historyTextArea);

        getContentPane().add(jScrollPane3);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(3);
        jTextArea1.setText("1) Set the corridor width.\n\n2) Place yourself at the start of the wall segment and press the 'Capture Point' button or shoot your gun once to capture your location.\n\n3) Go to the end of the wall segment and do the same.\n\n4) Walk at a tangent from the wall and capture your location once more to generate the corridor center line, displayed in the text area below.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea1);

        getContentPane().add(jScrollPane2);

        jPanel2.setMinimumSize(new java.awt.Dimension(176, 30));
        jPanel2.setPreferredSize(new java.awt.Dimension(313, 30));
        jPanel2.setLayout(new java.awt.GridLayout(5, 2));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Current Position:");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLabel1.setMaximumSize(new java.awt.Dimension(1000000, 14));
        jPanel2.add(jLabel1);

        currentPositionLabel.setText("position goes here");
        jPanel2.add(currentPositionLabel);

        jLabel2.setText("Wall Start:");
        jPanel2.add(jLabel2);

        firstPointLabel.setText("first");
        jPanel2.add(firstPointLabel);

        jLabel3.setText("Wall End:");
        jPanel2.add(jLabel3);

        secondPointLabel.setText("second");
        jPanel2.add(secondPointLabel);

        jLabel5.setText("Third Point:");
        jPanel2.add(jLabel5);

        thirdPointLabel.setText("third");
        jPanel2.add(thirdPointLabel);

        jLabel4.setText("Corridor Width:");
        jLabel4.setMaximumSize(new java.awt.Dimension(74, 20));
        jLabel4.setMinimumSize(new java.awt.Dimension(74, 20));
        jPanel2.add(jLabel4);
        jPanel2.add(corridorWidthTextBox);

        getContentPane().add(jPanel2);

        jPanel3.setMaximumSize(new java.awt.Dimension(32767, 23));
        jPanel3.setLayout(new java.awt.GridLayout(1, 0));

        capturePointButton.setText("Capture Segment Point");
        capturePointButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                capturePointButtonActionPerformed(evt);
            }
        });
        jPanel3.add(capturePointButton);

        getContentPane().add(jPanel3);

        jPanel5.setMaximumSize(new java.awt.Dimension(32767, 14));
        jPanel5.setLayout(new java.awt.GridLayout(2, 0));

        jLabel7.setText("First Point");
        jPanel5.add(jLabel7);

        firstLinePointLabel.setText("firstLinePoint");
        jPanel5.add(firstLinePointLabel);

        jLabel6.setText("Distance");
        jPanel5.add(jLabel6);

        lineDistanceLabel.setText("lineDistanceLabel");
        jPanel5.add(lineDistanceLabel);

        getContentPane().add(jPanel5);

        jPanel6.setMaximumSize(new java.awt.Dimension(32767, 23));
        jPanel6.setLayout(new java.awt.GridLayout(2, 0));

        captureLinePointButton.setText("Capture Line Point");
        captureLinePointButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureLinePointButtonActionPerformed(evt);
            }
        });
        jPanel6.add(captureLinePointButton);

        captureSinglePointButton.setText("Capture Single Point");
        captureSinglePointButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureSinglePointButtonActionPerformed(evt);
            }
        });
        jPanel6.add(captureSinglePointButton);

        getContentPane().add(jPanel6);

        outputTextArea.setColumns(20);
        outputTextArea.setRows(5);
        jScrollPane1.setViewportView(outputTextArea);

        getContentPane().add(jScrollPane1);

        jPanel4.setMaximumSize(new java.awt.Dimension(32767, 23));
        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

        saveFileButton.setText("Save File");
        saveFileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileButtonActionPerformed(evt);
            }
        });
        jPanel4.add(saveFileButton);

        getContentPane().add(jPanel4);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Takes the current position and applies to the next unset point
     */
    private void updatePoints() {
        if (firstPoint.getX() == 0.0) {
            firstPoint = getCurrentPoint();
        } else if (secondPoint.getX() == 0.0) {
            secondPoint = getCurrentPoint();
        } else if (thirdPoint.getX() == 0.0) {
            thirdPoint = getCurrentPoint();
            calculatePoints();
        } else {
            calculatePoints();
        }
    }

    private void capturePointButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_capturePointButtonActionPerformed
        updatePoints();
    }//GEN-LAST:event_capturePointButtonActionPerformed

    /**
     * Saves the file text area to a file
     *
     * @param evt The action event
     */
    private void saveFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileButtonActionPerformed
        int ret = saveFileChooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = saveFileChooser.getSelectedFile();
            try {
                FileWriter write = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(write);
                out.write(outputTextArea.getText());
                out.close();
            } catch (@SuppressWarnings("unused") IOException ex) {
            }
        }
    }//GEN-LAST:event_saveFileButtonActionPerformed

    private void captureSinglePointButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_captureSinglePointButtonActionPerformed
        outputTextArea.setText(outputTextArea.getText()
                + pointToString(getCurrentPoint()) + "\n");
    }//GEN-LAST:event_captureSinglePointButtonActionPerformed

    private void captureLinePointButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_captureLinePointButtonActionPerformed
        if (firstLinePoint == null) {
            firstLinePoint = getCurrentPoint();
        } else {
            Point3d secondLinePoint = getCurrentPoint();
            outputTextArea.setText(outputTextArea.getText()
                    + "firstLinePoint: " + pointToString(firstLinePoint) + "\n"
                    + "secondLinePoint: " + pointToString(secondLinePoint) + "\n");
            firstLinePoint = null;
        }
    }//GEN-LAST:event_captureLinePointButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton captureLinePointButton;
    private final javax.swing.JButton capturePointButton = new javax.swing.JButton();
    private javax.swing.JToggleButton captureSinglePointButton;
    private javax.swing.JTextField corridorWidthTextBox;
    private final javax.swing.JLabel currentPositionLabel = new javax.swing.JLabel();
    private javax.swing.JLabel firstLinePointLabel;
    private final javax.swing.JLabel firstPointLabel = new javax.swing.JLabel();
    private javax.swing.JTextArea historyTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lineDistanceLabel;
    private final javax.swing.JTextArea outputTextArea = new javax.swing.JTextArea();
    private final javax.swing.JButton saveFileButton = new javax.swing.JButton();
    private final javax.swing.JLabel secondPointLabel = new javax.swing.JLabel();
    private javax.swing.JComboBox<String> selectionComboBox;
    private final javax.swing.JLabel thirdPointLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables

    /**
     * A entity state packet has been received, stores the position as the
     * current
     *
     * @param msg An entity state packet
     */
    @Override
    public void entityStateReceived(Message msg) {
        EntityState es = (EntityState) msg.getPayload();
        Message original = entityIdToEntityState.put(es.getEntityID().getEntityID(), msg);
        String displayString = es.getEntityID().getEntityID()
                + " - " + es.getEntityType().getEntityKind()
                + "." + es.getEntityType().getDomain()
                + "." + es.getEntityType().getCountry()
                + "." + es.getEntityType().getCategory()
                + "." + es.getEntityType().getSubcategory()
                + "." + es.getEntityType().getSpecific()
                + "." + es.getEntityType().getExtra();
        if (original == null) {
            listOptionToEntityId.put(displayString, es.getEntityID().getEntityID());
            selectionComboBox.addItem(displayString);
        }
    }

    /**
     * A detonation packet has been received, captures the current position
     *
     * @param msg A detonation packet
     */
    @Override
    public void detonationReceived(Message msg) {
        Detonation det = (Detonation) msg.getPayload();
        historyTextArea.setText(det.getFiringEntityID().getEntityID() + " fired.\n" + historyTextArea.getText());
        if (currentEntityId == det.getFiringEntityID().getEntityID()) {
            updatePoints();
        }
    }

    /**
     * A weapon fire packet has been received
     *
     * @param msg A weapon fire packet
     */
    @Override
    public void weaponFireReceived(Message msg) {
        // Do nothing
    }
}
