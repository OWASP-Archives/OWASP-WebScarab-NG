/***********************************************************************
 *
 * $CVSHeader$
 *
 * This file is part of WebScarab, an Open Web Application Security
 * Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2004 Rogan Dawes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Getting Source
 * ==============
 *
 * Source for this application is maintained at Sourceforge.net, a
 * repository for free software projects.
 *
 * For details, please see http://www.sourceforge.net/projects/owasp
 *
 */

/*
 * HexEditor.java
 *
 * Created on November 4, 2003, 8:23 AM
 */

package org.owasp.webscarab.ui.editors;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.Font;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.Event;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author  rdawes
 */
public class HexPanel extends javax.swing.JPanel implements ByteArrayEditor {
    
	private static final long serialVersionUID = -3332802832419279428L;
	private HexTableModel _tableModel = null;
    private boolean _editable = false;
    private int _columns = 16;
    private boolean _modified = false;
    private byte[] _data = null;
    private byte[] _original = null;
    
    /** Creates new form HexEditor */
    public HexPanel() {
        initComponents();
        setName("Hex");
        
        _tableModel = new HexTableModel(_columns);
        hexTable.setModel(_tableModel);
        hexTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        hexTable.getTableHeader().setReorderingAllowed(false);
        TableColumnModel colModel = hexTable.getColumnModel();
        // FIXME : use FontMetrics to get the real width of the font
        for (int i=0; i<_columns+2; i++) {
            colModel.getColumn(i).setPreferredWidth(2*9);
            colModel.getColumn(i).setResizable(false);
        }
        colModel.getColumn(0).setPreferredWidth(8*9);
        colModel.getColumn(_columns+1).setPreferredWidth(_columns*9);
        InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK), "Save");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK), "Load");
        getActionMap().put("Save", new AbstractAction() {
			private static final long serialVersionUID = -872604689834638795L;

			public void actionPerformed(ActionEvent evt) {
                JFileChooser jfc = new JFileChooser();
                jfc.setDialogTitle("Select a file to write the message content to");
                int returnVal = jfc.showOpenDialog(HexPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        FileOutputStream fos = new FileOutputStream(jfc.getSelectedFile());
                        fos.write(_data);
                        fos.close();
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(HexPanel.this, "Error writing file: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        getActionMap().put("Load", new AbstractAction() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 7286272198340993109L;

			public void actionPerformed(ActionEvent evt) {
                if (!_editable) return;
                JFileChooser jfc = new JFileChooser();
                jfc.setDialogTitle("Select a file to read the message content from");
                int returnVal = jfc.showOpenDialog(HexPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        FileInputStream fis = new FileInputStream(jfc.getSelectedFile());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buff = new byte[2048];
                        int got;
                        while ((got = fis.read(buff))>0) {
                            baos.write(buff,0,got);
                        }
                        fis.close();
                        baos.close();
                        _data = baos.toByteArray();
                        _tableModel.fireTableDataChanged();
                        _modified = true;
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(HexPanel.this, "Error writing file: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }
    
    public String[] getContentTypes() {
        return new String[] { ".*" };
    }
    
    public void setEditable(boolean editable) {
        _editable = editable;
        hexTable.setBackground(editable ? Color.WHITE : Color.LIGHT_GRAY );
        // we could do things like make insert and delete buttons visible and invisible here
    }
    
    public void setBytes(String contentType, byte[] bytes) {
        _original = bytes;
        if (bytes == null) {
            _data = null;
        } else {
            _data = new byte[bytes.length];
            System.arraycopy(bytes, 0, _data, 0, bytes.length);
        }
        if (SwingUtilities.isEventDispatchThread()) {
            _tableModel.fireTableDataChanged();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    _tableModel.fireTableDataChanged();
                }
            });
        }
        _modified = false;
    }
    
    public boolean isModified() {
        if (hexTable.isEditing()) hexTable.getCellEditor().stopCellEditing();
        return _editable && _modified;
    }
    
    public byte[] getBytes() {
        if (_editable && isModified()) _original = _data;
        _modified = false;
        return _original;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        hexTable = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(453, 20));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(22, 48));
        hexTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        hexTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(hexTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable hexTable;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    
    private class HexTableModel extends AbstractTableModel {
        
		private static final long serialVersionUID = -3782965899741537329L;
		private int _columns = 8;
        
        public HexTableModel() {
        }
        
        public HexTableModel(int columns) {
            _columns = columns;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "Position";
            } else if (columnIndex-1 < _columns) {
                return Integer.toHexString(columnIndex-1).toUpperCase();
            } else {
                return "String";
            }
        }
        
        public int getColumnCount() {
            return _columns + 2;
        }
        
        public int getRowCount() {
            if (_data == null || _data.length == 0) {
                return 0;
            }
            if (_data.length % _columns == 0) {
                return (int) (_data.length / _columns);
            } else {
                return (int) (_data.length / _columns) + 1;
            }
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return pad(Integer.toHexString(rowIndex * _columns).toUpperCase(), '0', 8);
            } else if (columnIndex-1 < _columns) {
                int position = rowIndex * _columns + columnIndex-1;
                if (position < _data.length) {
                    int i = _data[position];
                    if (i<0) { i = i + 256; }
                    return pad(Integer.toString(i, 16).toUpperCase(),'0',2);
                } else {
                    return "";
                }
            } else {
                int start = rowIndex * _columns;
                StringBuffer buff = new StringBuffer();
                for (int i=0; i < _columns; i++) {
                    int pos = start + i;
                    if (pos >= _data.length) { return buff.toString(); }
                    if (_data[pos] < 32 || _data[pos] > 126) {
                        buff.append(".");
                    } else {
                        buff.append((char) _data[pos]);
                    }
                }
                return buff.toString();
            }
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0 || columnIndex>_columns) {
                return false;
            }
            int position = rowIndex * _columns + columnIndex-1;
            if (position < _data.length) {
                return _editable;
            }
            return false;
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            int position = rowIndex * _columns + columnIndex-1;
            if (position >= _data.length) {
                System.out.println("Out of range");
                return;
            }
            if (aValue instanceof String) {
                try {
                    String s = (String) aValue;
                    _data[position] = new Integer(Integer.parseInt(s.trim(), 16)).byteValue();
                    fireTableCellUpdated(rowIndex, _columns + 1);
                    _modified = true;
                } catch (NumberFormatException nfe) {
                    System.out.println("Number format error : " + nfe);
                }
            } else {
                System.out.println("Value is a " + aValue.getClass().getName());
            }
        }
        
        private String pad(String initial, char padchar, int length) {
            if (initial.length() >= length) {
                return initial;
            }
            StringBuffer buff = new StringBuffer(length);
            for (int i=0; i<length - initial.length(); i++) {
                buff.append(padchar);
            }
            buff.append(initial);
            return buff.toString();
        }
    }
    
}
