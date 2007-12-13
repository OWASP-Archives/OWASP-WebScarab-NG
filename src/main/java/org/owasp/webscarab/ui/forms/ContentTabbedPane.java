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
 * ContentTabbedPane.java
 *
 * Created on November 4, 2003, 8:06 AM
 */

package org.owasp.webscarab.ui.forms;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.util.ObjectUtils;

import javax.swing.JTabbedPane;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author  rdawes
 */
public class ContentTabbedPane extends JTabbedPane {
    
	private static final long serialVersionUID = -2640392392974249564L;
	
	private List<ContentForm> forms = new LinkedList<ContentForm>();
	
    private Listener listener;
    
    /** Creates new form ContentTabbedPane */
    public ContentTabbedPane(FormModel model, String headerProperty, String contentProperty) {
    	super();
    	forms.add(new ImageForm(model, headerProperty, contentProperty));
    	forms.add(new HtmlForm(model, headerProperty, contentProperty));
        forms.add(new UrlEncodedForm(model, headerProperty, contentProperty));
        forms.add(new UrlEncodedJsonForm(model, headerProperty, contentProperty));
        forms.add(new JsonForm(model, headerProperty, contentProperty));
        forms.add(new XmlForm(model, headerProperty, contentProperty));
        forms.add(new TextForm(model, headerProperty, contentProperty));
    	forms.add(new HexForm(model, headerProperty, contentProperty));
    	listener = new Listener();
    	NamedValue[] headers = (NamedValue[]) model.getValueModel(headerProperty).getValue();
    	String contentType = NamedValue.get("Content-Type", headers);
    	showForms(contentType);
    	model.getValueModel(headerProperty).addValueChangeListener(listener);
    	addHierarchyListener(listener);
    }
    
    private void showForms(String contentType) {
    	removeAll();
    	Iterator<ContentForm> it = forms.iterator();
    	while (it.hasNext()) {
    		ContentForm contentForm = it.next();
    		if (contentForm.canHandle(contentType)) {
    			addTab(contentForm.getId(), contentForm.getControl());
    		}
    	}
    }
    
    private String getContentType(String value) {
        if (value == null || value.length() == 0)
            return null;
        int semi = value.indexOf(';');
        if (semi > -1)
            return value.substring(0, semi).trim();
        return value;
    }
    
    private class Listener implements HierarchyListener, PropertyChangeListener {

    	private boolean upToDate = false;
    	
    	private String newContentType = null;
    	
		public void propertyChange(PropertyChangeEvent evt) {
			NamedValue[] headers = (NamedValue[]) evt.getOldValue();
			String oldContentType = getContentType(NamedValue.get("Content-Type", headers));
			headers = (NamedValue[]) evt.getNewValue();
			newContentType = getContentType(NamedValue.get("Content-Type", headers));
            if (! ObjectUtils.nullSafeEquals(oldContentType, newContentType)) {
			    upToDate = false;
				removeAll();
				if (isShowing()) {
					showForms(newContentType);
					upToDate = true;
				}
			}
		}

        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == 0)
                return;
            if (isShowing() && !upToDate) {
                showForms(newContentType);
                upToDate = true;
            }
        }

    }
    
}
