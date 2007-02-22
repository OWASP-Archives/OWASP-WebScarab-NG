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

import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.text.DefaultHighlighter;

import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Event;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author  rdawes
 */
public class TextPanel extends javax.swing.JPanel implements ByteArrayEditor {

	private static final long serialVersionUID = 7243325169523584635L;
	private static boolean _findVisible = false;
    private static String _find = "";
    private static boolean _caseSensitive = false;
    private int _start = 0;

    private boolean _editable = false;
    private boolean _modified = false;

    private String _text = null;

    private DocumentChangeListener _dcl = new DocumentChangeListener();

    private RegexSearcher searcher;

    /** Creates new form HexEditor */
    public TextPanel() {
        initComponents();
        findCaseCheckBox.setSelected(_caseSensitive);

        setName("Text");

        searcher = new RegexSearcher(textTextArea, DefaultHighlighter.DefaultPainter);

        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        getActionMap().put("TOGGLEWRAP", new AbstractAction() {
			private static final long serialVersionUID = -3377262704983025730L;

			public void actionPerformed(ActionEvent event) {
                textTextArea.setLineWrap(! textTextArea.getLineWrap());
            }
        });
        // Ctrl-W to toggle wordwrap
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK), "TOGGLEWRAP");

        getActionMap().put("TOGGLEFIND", new AbstractAction() {
			private static final long serialVersionUID = 3839729022016588962L;

			public void actionPerformed(ActionEvent event) {
                _findVisible = ! findPanel.isVisible();
                findPanel.setVisible(_findVisible);
                if (_findVisible) {
                    findTextField.requestFocusInWindow();
                }
            }
        });
        // Ctrl-F to toggle the find bar
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK), "TOGGLEFIND");

        findPanel.setVisible(_findVisible);
        findTextField.setText(_find);
        findTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent evt) {
                find();
            }
            public void removeUpdate(DocumentEvent evt) {
                find();
            }
            public void insertUpdate(DocumentEvent evt) {
                find();
            }
            private void find() {
                _find = findTextField.getText();
                _start = doFind(_find, 0, _caseSensitive) + 1;
            }
        });
    }

    public void setEditable(boolean editable) {
        _editable = editable;
        textTextArea.setEditable(editable);
    }

    public void setBytes(String contentType, byte[] bytes) {
        if (bytes == null) {
            setText(contentType, "");
        } else {
            try {
                setText(contentType, new String(bytes, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                setText(contentType, e.getMessage());
            }
        }
    }

    public void setText(String contentType, String text) {
//        String wrap = "false";
//        if (wrap != null && wrap.equals("true")) textTextArea.setLineWrap(true);

        _text = text;
        textTextArea.getDocument().removeDocumentListener(_dcl);
        _modified = false;
        if (text != null)
            textTextArea.setText(text);
        textTextArea.setCaretPosition(0);
        if (_editable)
            textTextArea.getDocument().addDocumentListener(_dcl);
        _start = doFind(_find, 0, _caseSensitive) + 1;
    }

    public String getText() {
        if (_text == null)
            _text = textTextArea.getText();
        return _text;
    }

    public boolean isModified() {
        return _editable && _modified;
    }

    public byte[] getBytes() {
        try {
            return getText().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("This should never happen!");
            return null;
        }
    }

    private int doFind(String pattern, int start, boolean caseSensitive) {
        findMessageLabel.setText("");
        try {
            int pos = searcher.search(pattern, start, caseSensitive);
            if (start > 0 && pos == -1) {
                pos = searcher.search(pattern, 0, caseSensitive);
                if (pos > -1)
                    findMessageLabel.setText("Reached end of page, continued from top");
            }
            if (pos > -1) {
                textTextArea.setCaretPosition(pos);
            } else {
                findMessageLabel.setText("Not found");
            }
            return pos;
        } catch (PatternSyntaxException pse) {
            findMessageLabel.setText(pse.getMessage());
            return -1;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        textScrollPane = new javax.swing.JScrollPane();
        textTextArea = new javax.swing.JTextArea();
        findPanel = new javax.swing.JPanel();
        findLabel = new javax.swing.JLabel();
        findTextField = new javax.swing.JTextField();
        findNextButton = new javax.swing.JButton();
        findCaseCheckBox = new javax.swing.JCheckBox();
        findMessageLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        textTextArea.setEditable(false);
        textScrollPane.setViewportView(textTextArea);

        add(textScrollPane, java.awt.BorderLayout.CENTER);

        findPanel.setLayout(new java.awt.GridBagLayout());

        findLabel.setText("Find: ");
        findPanel.add(findLabel, new java.awt.GridBagConstraints());

        findTextField.setMinimumSize(new java.awt.Dimension(60, 19));
        findTextField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        findPanel.add(findTextField, gridBagConstraints);

        findNextButton.setMnemonic('N');
        findNextButton.setText("Next");
        findNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findNextButtonActionPerformed(evt);
            }
        });

        findPanel.add(findNextButton, new java.awt.GridBagConstraints());

        findCaseCheckBox.setMnemonic('M');
        findCaseCheckBox.setText("Match Case");
        findCaseCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findCaseCheckBoxActionPerformed(evt);
            }
        });

        findPanel.add(findCaseCheckBox, new java.awt.GridBagConstraints());

        findMessageLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        findPanel.add(findMessageLabel, gridBagConstraints);

        add(findPanel, java.awt.BorderLayout.SOUTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void findCaseCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findCaseCheckBoxActionPerformed
        _caseSensitive = findCaseCheckBox.isSelected();
        _start = doFind(_find, 0, _caseSensitive) + 1;
    }//GEN-LAST:event_findCaseCheckBoxActionPerformed

    private void findNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findNextButtonActionPerformed
        _start = doFind(_find, _start, _caseSensitive) + 1;
    }//GEN-LAST:event_findNextButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox findCaseCheckBox;
    private javax.swing.JLabel findLabel;
    private javax.swing.JLabel findMessageLabel;
    private javax.swing.JButton findNextButton;
    private javax.swing.JPanel findPanel;
    private javax.swing.JTextField findTextField;
    private javax.swing.JScrollPane textScrollPane;
    private javax.swing.JTextArea textTextArea;
    // End of variables declaration//GEN-END:variables

    private class DocumentChangeListener implements DocumentListener {
        public void changedUpdate(DocumentEvent evt) {
            _modified = true;
            _text = null;
        }
        public void removeUpdate(DocumentEvent evt) {
            _modified = true;
            _text = null;
        }
        public void insertUpdate(DocumentEvent evt) {
            _modified = true;
            _text = null;
        }
    }

}
