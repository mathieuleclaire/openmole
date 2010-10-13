/*
 *
 *  Copyright (c) 2008, Cemagref
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation; either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston,
 *  MA  02110-1301  USA
 */
package org.simexplorer.ide.ui;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ToolTipManager;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openmole.core.implementation.task.Task;
import org.simexplorer.core.workflow.model.metada.MetadataLoader;
import org.openmole.core.implementation.task.GenericTask;
import org.simexplorer.core.workflow.model.Duo;
import org.simexplorer.core.workflow.model.metada.Metadata;

public class ProcessorChooserPanel extends javax.swing.JPanel {
    private static final Logger LOGGER = Logger.getLogger(ProcessorChooserPanel.class.getName());

    private Map<String, Duo<Class, Metadata>> processorsByName;
    private DialogDescriptor dialogDescriptor;
    private static String helpMetaDataKey = NbBundle.getMessage(ProcessorChooserPanel.class, "ProcessorChooserPanel.helpMetaDataKey");
    private static String nohelpProvided = NbBundle.getMessage(ProcessorChooserPanel.class, "ProcessorChooserPanel.nohelpProvided");

    public ProcessorChooserPanel(GenericTask task) {
        processorsByName = new HashMap<String, Duo<Class, Metadata>>();
        List<Class<? extends Task>> processors = ServicesProvider.getProcessors(task.getName());
        for (Class<? extends Task> processor : processors) {
            Metadata metadata = MetadataLoader.loadMetadata(processor);
            processorsByName.put(metadata.get("name"), new Duo<Class, Metadata>(processor, metadata));
        }
        initComponents();
        processorsComboBox.setRenderer(new TaskComboBoxRenderer());
        processorsComboBox.setSelectedIndex(-1);
    }

    public Task getProcessor() {
        Class processorType = processorsByName.get(processorsComboBox.getSelectedItem()).getLeft();
        LOGGER.fine("Creating a new object task of type " + processorType + "…");
        try {
            Task p = (Task) processorType.getDeclaredConstructor(String.class).newInstance(processorType.getSimpleName());
            // TODO description isn't possible for instance
            // MetadataProxy.setMetadata(p, "description", jTextAreaDescription.getText());
            return p;
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        processorsComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaDescription = new javax.swing.JTextArea();

        processorsComboBox.setModel(new DefaultComboBoxModel(processorsByName.keySet().toArray()));
        processorsComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessorChooserPanel.class, "ProcessorChooserPanel.processorsComboBox.toolTipText")); // NOI18N
        processorsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processorsComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ProcessorChooserPanel.class, "ProcessorChooserPanel.jLabel1.text")); // NOI18N

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(ProcessorChooserPanel.class, "ProcessorChooserPanel.descriptionLabel.text")); // NOI18N

        jTextAreaDescription.setColumns(20);
        jTextAreaDescription.setEditable(false);
        jTextAreaDescription.setRows(5);
        jTextAreaDescription.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessorChooserPanel.class, "ProcessorChooserPanel.jTextAreaDescription.toolTipText")); // NOI18N
        jTextAreaDescription.setEnabled(false);
        jScrollPane1.setViewportView(jTextAreaDescription);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(processorsComboBox, 0, 136, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(processorsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(descriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void processorsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processorsComboBoxActionPerformed
        if (processorsComboBox.getSelectedIndex() > -1) {
            String description = processorsByName.get(processorsComboBox.getSelectedItem()).getRight().get("description");
            if (description != null) {
                descriptionLabel.setText("<html>" + description + "</html>");
            } else {
                descriptionLabel.setText("");
            }
            if (dialogDescriptor != null) {
                dialogDescriptor.setValid(true);
            }
        }
    }//GEN-LAST:event_processorsComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaDescription;
    private javax.swing.JComboBox processorsComboBox;
    // End of variables declaration//GEN-END:variables

    class TaskComboBoxRenderer extends JLabel implements ListCellRenderer {

        public TaskComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            if (value == null) {
                setText("");
            } else {
                String text = "<html><body>" + value.toString() + "</body></html>";
                String description = (processorsByName.get(list.getModel().getElementAt(index)) != null) ? //                String description = (processorsByName.get(processorsComboBox.getSelectedItem())!=null)?
                        processorsByName.get(list.getModel().getElementAt(index)).getRight().get(helpMetaDataKey) : null;

                description = (description != null) ? description : nohelpProvided;
                String toolTip = "<html><body>  <b>" + value.toString() + "</b> <i>" + description + "</i>  </body></html>";
                setToolTipText(toolTip);
// forces tooltip staying on top
                ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
                list.setOpaque(true);
                setText(text);
            }
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return this;
        }
    }
}
