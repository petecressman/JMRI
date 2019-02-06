/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.code.JMRIConnection;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.jmrit.ctc.ctcserialdata.OtherData;

/**
 *
 * @author NetBeansJMRI
 */
public class DlgJMRISimpleServerParams extends javax.swing.JDialog {
    private static final String FORM_PROPERTIES = "DlgJMRIServerParams";
    private final AwtWindowProperties _mAwtWindowProperties;
    private final OtherData _mOtherData;
    private final JMRIConnection _mJMRIConnection;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }

    private String  _mJMRI_HostOrig;
    private int     _mJMRI_PortOrig;
    private void initOrig(OtherData otherData) {
        _mJMRI_HostOrig = otherData._mJMRI_Host;
        _mJMRI_PortOrig = otherData._mJMRI_Port;
    }
    
    private boolean dataChanged() {
        if (!_mJMRI_HostOrig.equals(_mJMRI_Host.getText())) return true;
        if (_mJMRI_PortOrig != (int)_mJMRI_Port.getValue()) return true;
        return false;
    }
    
    public DlgJMRISimpleServerParams(java.awt.Frame parent, boolean modal, AwtWindowProperties awtWindowProperties, OtherData otherData, JMRIConnection jmriConnection) {
        super(parent, modal);
        _mAwtWindowProperties = awtWindowProperties;
        _mOtherData = otherData;
        _mJMRIConnection = jmriConnection;
        initComponents();
        initOrig(otherData);
        _mJMRI_Host.setText(otherData._mJMRI_Host);
        _mJMRI_Port.setModel(new SpinnerNumberModel(otherData._mJMRI_Port, 0, 65535, 1));
        _mJMRI_Port.setEditor(new JSpinner.NumberEditor(_mJMRI_Port, "#"));
        _mAwtWindowProperties.setWindowState((java.awt.Window)this, FORM_PROPERTIES);        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mSaveAndClose = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        _mJMRI_Host = new javax.swing.JTextField();
        _mJMRI_Port = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("JMRI Simple Server Parameters:");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _mSaveAndClose.setText("Save and close");
        _mSaveAndClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        jLabel1.setText("Host:");

        jLabel2.setText("Port:");

        jLabel3.setText("(Default is 2048)");

        jLabel4.setText("Use localhost or 127.0.0.1 for same computer");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mJMRI_Host, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mJMRI_Port, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(140, 140, 140)
                        .addComponent(_mSaveAndClose)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(_mJMRI_Host, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mJMRI_Port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addComponent(_mSaveAndClose)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        _mOtherData._mJMRI_Host = _mJMRI_Host.getText();
        _mOtherData._mJMRI_Port = (int)_mJMRI_Port.getValue();
        if (dataChanged()) { // Need to reconnect!
            _mJMRIConnection.reconnect(_mOtherData._mJMRI_Host, _mOtherData._mJMRI_Port);
        }
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField _mJMRI_Host;
    private javax.swing.JSpinner _mJMRI_Port;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}
