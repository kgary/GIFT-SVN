/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.segmentgenerator;

import mil.arl.gift.common.io.ImageUtil;

import org.jdis.pdu.record.WorldCoordinates;

/**
 *
 * @author jleonard
 */
public class DISEntityGeneratorFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    private WorldCoordinates entityPosition = null;

    private VbsEntityEnum entityType = null;

    /** Creates new form DISEntityGeneratorFrame */
    public DISEntityGeneratorFrame() {
        initComponents();
        
        setIconImage(ImageUtil.getInstance().getSystemIcon());
        
        for (VbsEntityEnum i : VbsEntityEnum.VALUES()) {
            entityTypeComboBox.addItem(i.getName());
        }
    }

    public WorldCoordinates getEntityPosition() {
        return this.entityPosition;
    }

    private void emitEntityStatePDU() {
        updateEntityPosition();
        updateEntityType();
        EntityGenerator.getInstance().emitEntityState(entityPosition, entityType);
    }

    private void updateEntityPosition() {
        String pos = entityPositionTextField.getText();
        String[] posComp = pos.split(",");
        entityPosition = new WorldCoordinates(Double.valueOf(posComp[0]), Double.valueOf(posComp[1]), Double.valueOf(posComp[2]));
    }

    private void updateEntityType() {
        entityType = VbsEntityEnum.valueOf((String) entityTypeComboBox.getSelectedItem());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        entityPositionTextField = new javax.swing.JTextField();
        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        emitButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setTitle("Entity Generator");

        jLabel1.setText("Enity Position");

        entityPositionTextField.addCaretListener(new javax.swing.event.CaretListener() {
            @Override
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                entityPositionTextFieldCaretUpdate(evt);
            }
        });

        startButton.setText("Start");

        stopButton.setText("Stop");
        stopButton.setEnabled(false);

        emitButton.setText("Emit");
        emitButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emitButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Entity Type");

        entityTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                entityTypeComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(emitButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(entityTypeComboBox, 0, 244, Short.MAX_VALUE)
                            .addComponent(entityPositionTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(entityPositionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(entityTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopButton)
                    .addComponent(startButton)
                    .addComponent(emitButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void emitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emitButtonActionPerformed
        emitEntityStatePDU();
    }//GEN-LAST:event_emitButtonActionPerformed

    private void entityPositionTextFieldCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_entityPositionTextFieldCaretUpdate
        updateEntityPosition();
    }//GEN-LAST:event_entityPositionTextFieldCaretUpdate

    private void entityTypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_entityTypeComboBoxItemStateChanged
        updateEntityType();
    }//GEN-LAST:event_entityTypeComboBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton emitButton;
    private javax.swing.JTextField entityPositionTextField;
    private final javax.swing.JComboBox<String> entityTypeComboBox = new javax.swing.JComboBox<String>();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables
}
