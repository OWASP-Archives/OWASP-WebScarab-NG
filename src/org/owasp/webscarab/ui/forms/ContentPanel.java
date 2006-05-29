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
 * ContentPanel.java
 *
 * Created on November 4, 2003, 8:06 AM
 */

package org.owasp.webscarab.ui.forms;

import java.util.List;
import java.util.ArrayList;

import org.owasp.webscarab.ui.editors.ByteArrayEditor;
import org.owasp.webscarab.ui.editors.EditorFactory;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 *
 * @author  rdawes
 */
public class ContentPanel extends javax.swing.JPanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2640392392974249564L;
	private String _contentType = null;
    private boolean _editable = false;
    private boolean _modified = false;
    
    private byte[] _data = null;
    
    private ByteArrayEditor[] _editors = null;
    
    private int _selected = -1;
    private boolean[] _upToDate = new boolean[] {false};
    
    // This list is sorted in increasing order of preference
    private static List _preferred = new ArrayList();
    private boolean _creatingPanels = false;
    
    /** Creates new form ContentPanel */
    public ContentPanel() {
        initComponents();
        viewTabbedPane.addChangeListener(new ChangeListener() {
            @SuppressWarnings({"unchecked","unchecked"})
			public void stateChanged(ChangeEvent e) {
                if (_creatingPanels) return;
                // update our view of the data, after (possible) modifications
                // in the previously selected editor
                updateData(_selected);
                _selected = viewTabbedPane.getSelectedIndex();
                if (_selected>-1) {
                    updatePanel(_selected);
                    String name = _editors[_selected].getName();
                    _preferred.remove(name);
                    _preferred.add(name);
                }
            }
        });
    }
    
    public void setEditable(boolean editable) {
        _editable = editable;
        if (_editors != null) {
            for (int i=0; i<_editors.length; i++) {
                _editors[i].setEditable(editable);
            }
        }
    }
    
    public void setContentType(String contentType) {
    	System.out.println("Content-Type is " + contentType);
        if (_contentType == null || !_contentType.equals(contentType)) {
            _contentType = contentType;
            createPanels(_contentType);
        }
    }
    
    private void createPanels(final String contentType) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try { 
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        createPanels(contentType);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            _creatingPanels = true;
            viewTabbedPane.removeAll();
            _editors = EditorFactory.getEditors(contentType);
            for (int i=0; i<_editors.length; i++) {
                _editors[i].setEditable(_editable);
                viewTabbedPane.add((Component)_editors[i]);
            }
            int preferred = -1;
            for (int i=0; i<_preferred.size(); i++) {
                for (int e=0; e<_editors.length; e++) {
                    if (_editors[e].getName().equals(_preferred.get(i))) {
                        preferred = e;
                        break;
                    }
                }
            }
            invalidateEditors();
            revalidate();
            if (preferred > -1) {
                viewTabbedPane.setSelectedIndex(preferred);
            }
            _creatingPanels = false;
        }
    }
    
    private void invalidateEditors() {
        _upToDate = new boolean[_editors.length];
        for (int i=0; i<_upToDate.length; i++)
            _upToDate[i] = false;
    }
    
    public void setContent(byte[] content) {
        _modified = false;
        if (content == null) {
            _data = null;
        } else {
            _data = new byte[content.length];
            System.arraycopy(content, 0, _data, 0, content.length);
        }
        
        if (_editors == null || _editors.length == 0) {
            return;
        }
        invalidateEditors();
        
        _selected = viewTabbedPane.getSelectedIndex();
        if (_selected < 0) {
            _selected = 0;
            viewTabbedPane.setSelectedIndex(_selected);
        }
        updatePanel(_selected);
    }
    
    public boolean isModified() {
        if (! _editable) return false;
        int selected = viewTabbedPane.getSelectedIndex();
        if (selected < 0) return false;
        return _modified || _editors[selected].isModified();
    }
    
    public byte[] getContent() {
        if (isModified()) {
            int selected = viewTabbedPane.getSelectedIndex();
            _data = _editors[selected].getBytes();
            _modified = false;
        }
        return _data;
    }
    
    private void updatePanel(int panel) {
        if (panel<0 || _upToDate.length == 0) {
            return;
        } else if (panel >= _upToDate.length) {
            panel = 0;
        }
        if (!_upToDate[panel]) {
            _editors[panel].setBytes(_contentType, _data);
            _upToDate[panel] = true;
        }
    }
    
    private void updateData(int panel) {
        if (_editable && panel >= 0) {
            ByteArrayEditor ed = (ByteArrayEditor) viewTabbedPane.getComponentAt(panel);
            if (ed.isModified()) {
                _modified = true;
                _data = ed.getBytes();
                invalidateEditors();
                _upToDate[panel] = true;
            }
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

        viewTabbedPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        viewTabbedPane.setPreferredSize(new java.awt.Dimension(300, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(viewTabbedPane, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane viewTabbedPane;
    // End of variables declaration//GEN-END:variables
    
}
