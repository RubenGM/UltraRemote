/*
 * Copyright 2023 ArSi (arsi_at_arsi_sk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.arsi.saturn.ultra.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import sk.arsi.saturn.ultra.sender.pojo.Attributes.AttrDeserializer;
import sk.arsi.saturn.ultra.sender.pojo.Attributes.AttrRoot;
import sk.arsi.saturn.ultra.sender.pojo.Browse.BrowseRoot;
import sk.arsi.saturn.ultra.sender.pojo.Browse.BrowseRootDeserializer;
import sk.arsi.saturn.ultra.sender.pojo.Status.StatusDeserializer;
import sk.arsi.saturn.ultra.sender.pojo.Status.StatusRoot;

/**
 *
 * @author arsi
 */
public class PrinterBrowser extends javax.swing.JPanel {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final JFrame frame;
    private final String filename;
    /**
     * Creates new form PrinterBrowser
     */
    public PrinterBrowser(JFrame frame, String filename) {
        initComponents();
        this.filename = filename;
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StatusRoot.class, new StatusDeserializer());
        module.addDeserializer(BrowseRoot.class, new BrowseRootDeserializer());
        module.addDeserializer(AttrRoot.class, new AttrDeserializer());
        MAPPER.registerModule(module);
        select.setEnabled(false);
        this.frame = frame;
        list.setCellRenderer(new ListCellRenderer<JPanel>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends JPanel> list, JPanel value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel renderer = (JPanel) value;
                renderer.setBackground(isSelected ? Color.LIGHT_GRAY : list.getBackground());
                return renderer;
            }
        });
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JPanel selectedValue = list.getSelectedValue();
                    if (selectedValue != null) {
                        select.setEnabled(true);
                    } else {
                        select.setEnabled(false);
                    }
                }
            }
        });
        findPrinterActionPerformed(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        findPrinter = new javax.swing.JButton();
        select = new javax.swing.JButton();

        jScrollPane1.setViewportView(list);

        findPrinter.setText("Find printer");
        findPrinter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findPrinterActionPerformed(evt);
            }
        });

        select.setText("Select");
        select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(findPrinter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(select, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(findPrinter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(select)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void findPrinterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findPrinterActionPerformed
        try (DatagramSocket socket = new DatagramSocket(3000)) {
            // TODO add your handling code here:
            DefaultListModel<JPanel> model = new DefaultListModel<>();
            list.setModel(model);
            socket.setSoTimeout(2000);
            socket.setReuseAddress(true);
            System.out.println("M99999");
            String browse = "M99999";
            DatagramPacket packet = null;
            DatagramPacket reply = null;
            packet = new DatagramPacket(browse.getBytes(), browse.getBytes().length, InetAddress.getByName("255.255.255.255"), 3000);
            socket.send(packet);
            try {
                do {
                    byte[] data = new byte[1024];
                    reply = new DatagramPacket(data, data.length);
                    socket.receive(reply);
                    if (reply.getLength() > 6) {
                        String json = new String(reply.getData(), reply.getOffset(), reply.getLength(), "UTF-8");
                        BrowseRoot readValue = MAPPER.readValue(json, BrowseRoot.class);
                        model.addElement(new Printer(readValue));
                        System.out.println(json);
                    }

                } while (true);
            } catch (IOException iOException) {
              //  Logger.getLogger(PrinterBrowser.class.getName()).log(Level.SEVERE, null, iOException);
            }
        } catch (SocketException ex) {
            Logger.getLogger(PrinterBrowser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(PrinterBrowser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrinterBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_findPrinterActionPerformed

    private void selectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectActionPerformed
        // TODO add your handling code here:
        frame.setContentPane(new Detail(((Printer)list.getSelectedValue()).getRoot(),filename));
        frame.revalidate();
        frame.repaint();
    }//GEN-LAST:event_selectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton findPrinter;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<JPanel> list;
    private javax.swing.JButton select;
    // End of variables declaration//GEN-END:variables
}