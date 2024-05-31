/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.segmentgenerator;

import mil.arl.gift.common.io.ImageUtil;
import geotransform.coords.Gcc_Coord_3d;
import geotransform.coords.Gdc_Coord_3d;
import geotransform.coords.Utm_Coord_3d;
import geotransform.ellipsoids.WE_Ellipsoid;
import geotransform.transforms.Gcc_To_Gdc_Converter;
import geotransform.transforms.Gcc_To_Utm_Converter;
import geotransform.transforms.Gdc_To_Gcc_Converter;


/**
 *
 * @author jleonard
 */
public class CoordinateConverterFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    
    /** Creates new form CoordinateConverterFrame */
    public CoordinateConverterFrame() {
        initComponents();
        
        setIconImage(ImageUtil.getInstance().getSystemIcon());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fromCoordinateComboBox = new javax.swing.JComboBox<>();
        toCoordinateComboBox = new javax.swing.JComboBox<>();
        convertButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fromCoordinateTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        resultTextArea = new javax.swing.JTextArea();

        setTitle("Coordinate Converter");

        fromCoordinateComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "GCC", "GDC" }));

        toCoordinateComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UTM", "GDC", "GCC" }));

        convertButton.setText("Convert");
        convertButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("From");

        jLabel2.setText("To");

        fromCoordinateTextArea.setColumns(20);
        fromCoordinateTextArea.setLineWrap(true);
        fromCoordinateTextArea.setRows(2);
        fromCoordinateTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(fromCoordinateTextArea);

        resultTextArea.setColumns(20);
        resultTextArea.setLineWrap(true);
        resultTextArea.setRows(2);
        resultTextArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(resultTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(convertButton, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fromCoordinateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(toCoordinateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(fromCoordinateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(toCoordinateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(convertButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void convertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertButtonActionPerformed
        String[] coordinateStrings = fromCoordinateTextArea.getText().split(",");
        
        if(coordinateStrings.length == 3) {

            Double[] coordinateValues = new Double[3];
            for (int i = 0; i < 3; ++i) {
                coordinateValues[i] = Double.parseDouble(coordinateStrings[i]);
            }
            
            if(fromCoordinateComboBox.getSelectedItem().equals("GCC") && toCoordinateComboBox.getSelectedItem().equals("GDC")) {
                Gcc_Coord_3d gcc = new Gcc_Coord_3d(coordinateValues[0],coordinateValues[1],coordinateValues[2]);
                Gdc_Coord_3d gdc = new Gdc_Coord_3d();
                Gcc_To_Gdc_Converter.Init(new WE_Ellipsoid());
                Gcc_To_Gdc_Converter.Convert(gcc, gdc);
                
                String lat = "N";
                if(gdc.latitude < 0) {
                    lat = "S";
                }
                
                String lon = "E";
                if(gdc.longitude < 0) {
                    lon = "W";
                }
                
                resultTextArea.setText(Math.abs(gdc.latitude)+lat+","+Math.abs(gdc.longitude)+lon);
                
            } else if(fromCoordinateComboBox.getSelectedItem().equals("GCC") && toCoordinateComboBox.getSelectedItem().equals("UTM")) {
                Gcc_Coord_3d gcc = new Gcc_Coord_3d(coordinateValues[0],coordinateValues[1],coordinateValues[2]);
                Utm_Coord_3d utm = new Utm_Coord_3d();
                Gcc_To_Utm_Converter.Init(new WE_Ellipsoid());
                Gcc_To_Utm_Converter.Convert(gcc, utm);
                                
                resultTextArea.setText(utm.x+","+utm.y+","+utm.z);
                
            } else if(fromCoordinateComboBox.getSelectedItem().equals("GDC") && toCoordinateComboBox.getSelectedItem().equals("GCC")) {
                Gdc_Coord_3d gdc = new Gdc_Coord_3d(coordinateValues[0],coordinateValues[1],coordinateValues[2]);
                Gcc_Coord_3d gcc = new Gcc_Coord_3d();
                Gdc_To_Gcc_Converter.Init(new WE_Ellipsoid());
                Gdc_To_Gcc_Converter.Convert(gdc, gcc);
                
                resultTextArea.setText(gcc.x+","+gcc.y+","+gcc.z);
                
            } else {
                resultTextArea.setText("Not implemented yet.");
            }
        }
    }//GEN-LAST:event_convertButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton convertButton;
    private javax.swing.JComboBox<String> fromCoordinateComboBox;
    private javax.swing.JTextArea fromCoordinateTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea resultTextArea;
    private javax.swing.JComboBox<String> toCoordinateComboBox;
    // End of variables declaration//GEN-END:variables
}
