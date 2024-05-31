/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.segmentgenerator;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.dis.DISInterface;

/**
 *
 * @author jleonard
 */
public class GatewayToolFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    private static GatewayToolFrame instance = null;

    private ToolGatewayModule gatewayModule = new ToolGatewayModule();

    private SegmentGeneratorFrame segmentGenerator = null;

    private DISEntityGeneratorFrame entityGenerator = null;

    private CoordinateConverterFrame coordinateConverter = null;

    private EntityStatusTableFrame entityStatusTable = null;

    /** Creates new form GatewayToolFrame */
    private GatewayToolFrame() {
        initComponents();
        
        setIconImage(ImageUtil.getInstance().getSystemIcon());
        
        gatewayModule.init();

        List<DISInterface> disInterfaces = new ArrayList<DISInterface>();
        for (AbstractInteropInterface i : gatewayModule.getInterops().values()) {
            if (i instanceof DISInterface) {
                disInterfaces.add((DISInterface) i);
            }
        }

        EntityGenerator.getInstance().config(disInterfaces);
    }

    public static GatewayToolFrame getInstance() {
        if (instance == null) {
            instance = new GatewayToolFrame();
            instance.setVisible(true);
        }
        return instance;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        coordinateConverterButton = new javax.swing.JButton();
        disEntityGeneratorButton = new javax.swing.JButton();
        segmentGeneratorButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        pduOnEntityStateCheckBox = new javax.swing.JCheckBox();
        pduOnWeaponFireCheckBox = new javax.swing.JCheckBox();
        pduOnDetonationCheckBox = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();

        jLabel2.setText("jLabel2");

        setTitle("Gateway Tool");

        coordinateConverterButton.setText("Coordinate Converter");
        coordinateConverterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                coordinateConverterButtonActionPerformed(evt);
            }
        });

        disEntityGeneratorButton.setText("DIS Entity Generator");
        disEntityGeneratorButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disEntityGeneratorButtonActionPerformed(evt);
            }
        });

        segmentGeneratorButton.setText("Segment Generator");
        segmentGeneratorButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segmentGeneratorButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Tools");

        pduOnEntityStateCheckBox.setText("Emit PDU on Entity State");
        pduOnEntityStateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pduOnEntityStateCheckBoxActionPerformed(evt);
            }
        });

        pduOnWeaponFireCheckBox.setText("Emit PDU on Weapon Fire");
        pduOnWeaponFireCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pduOnWeaponFireCheckBoxActionPerformed(evt);
            }
        });

        pduOnDetonationCheckBox.setText("Emit PDU on Detonation");
        pduOnDetonationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pduOnDetonationCheckBoxActionPerformed(evt);
            }
        });

        jLabel3.setText("Options");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pduOnDetonationCheckBox)
                    .addComponent(pduOnWeaponFireCheckBox)
                    .addComponent(pduOnEntityStateCheckBox)
                    .addComponent(coordinateConverterButton, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                    .addComponent(disEntityGeneratorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                    .addComponent(segmentGeneratorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pduOnEntityStateCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pduOnWeaponFireCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pduOnDetonationCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(coordinateConverterButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(disEntityGeneratorButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(segmentGeneratorButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void coordinateConverterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_coordinateConverterButtonActionPerformed
        if (coordinateConverter == null) {
            coordinateConverter = new CoordinateConverterFrame();
        }
        coordinateConverter.setVisible(true);
    }//GEN-LAST:event_coordinateConverterButtonActionPerformed

    private void disEntityGeneratorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disEntityGeneratorButtonActionPerformed
        if (entityGenerator == null) {
            entityGenerator = new DISEntityGeneratorFrame();
        }
        entityGenerator.setVisible(true);
    }//GEN-LAST:event_disEntityGeneratorButtonActionPerformed

    private void segmentGeneratorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segmentGeneratorButtonActionPerformed
        if (segmentGenerator == null) {
            segmentGenerator = new SegmentGeneratorFrame();
            gatewayModule.addListener(segmentGenerator);
        }
        if (entityStatusTable == null) {
            entityStatusTable = new EntityStatusTableFrame();
            gatewayModule.addListener(entityStatusTable);
        }

        segmentGenerator.setVisible(true);
        entityStatusTable.setVisible(true);
    }//GEN-LAST:event_segmentGeneratorButtonActionPerformed

    private void pduOnEntityStateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pduOnEntityStateCheckBoxActionPerformed
        gatewayModule.setPduOnEntityState(pduOnEntityStateCheckBox.isSelected());
    }//GEN-LAST:event_pduOnEntityStateCheckBoxActionPerformed

    private void pduOnWeaponFireCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pduOnWeaponFireCheckBoxActionPerformed
        gatewayModule.setPduOnWeaponFire(pduOnWeaponFireCheckBox.isSelected());
    }//GEN-LAST:event_pduOnWeaponFireCheckBoxActionPerformed

    private void pduOnDetonationCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pduOnDetonationCheckBoxActionPerformed
        gatewayModule.setPduOnDetonation(pduOnDetonationCheckBox.isSelected());
    }//GEN-LAST:event_pduOnDetonationCheckBoxActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        GatewayToolFrame.getInstance();

        // Wait until the frame disappears to close the application
        while (GatewayToolFrame.getInstance().isVisible()) {
            try {
                Thread.sleep(1);
            } catch (@SuppressWarnings("unused") InterruptedException ex) {
            }
        }

        System.out.println("Good-bye");

        //Kill any threads
        System.exit(0);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton coordinateConverterButton;
    private javax.swing.JButton disEntityGeneratorButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JCheckBox pduOnDetonationCheckBox;
    private javax.swing.JCheckBox pduOnEntityStateCheckBox;
    private javax.swing.JCheckBox pduOnWeaponFireCheckBox;
    private javax.swing.JButton segmentGeneratorButton;
    // End of variables declaration//GEN-END:variables
}
