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
import java.util.LinkedHashMap;
import java.util.Map;

import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.util.ObjectUtils;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author  rdawes
 */
public class ContentTabbedPane extends JTabbedPane {
    
	private static final long serialVersionUID = -2640392392974249564L;
	
	private Map<ContentForm, JComponent> forms = new LinkedHashMap<ContentForm, JComponent>();
	
    private Listener listener;
    
    /** Creates new form ContentTabbedPane */
    public ContentTabbedPane(FormModel model, String headerProperty, String contentProperty) {
    	super();
    	forms.put(new ImageForm(model, contentProperty), null);
    	forms.put(new HtmlForm(model, contentProperty), null);
    	forms.put(new TextForm(model, contentProperty), null);
    	forms.put(new HexForm(model, contentProperty), null);
    	listener = new Listener();
    	NamedValue[] headers = (NamedValue[]) model.getValueModel(headerProperty).getValue();
    	String contentType = NamedValue.get("Content-Type", headers);
    	showForms(contentType);
    	model.getValueModel(headerProperty).addValueChangeListener(listener);
    	addComponentListener(listener);
    }
    
    private void showForms(String contentType) {
    	removeAll();
    	Iterator<ContentForm> it = forms.keySet().iterator();
    	while (it.hasNext()) {
    		ContentForm contentForm = it.next();
    		if (contentForm.canHandle(contentType)) {
    			JComponent control = forms.get(contentForm);
    			if (control == null) {
    				control = contentForm.getControl();
    				forms.put(contentForm, control);
    			}
    			addTab(contentForm.getId(), control);
    		}
    	}
    }
    
    private class Listener extends ComponentAdapter implements PropertyChangeListener {

    	private boolean upToDate = false;
    	
    	private String newContentType = null;
    	
		public void propertyChange(PropertyChangeEvent evt) {
			NamedValue[] headers = (NamedValue[]) evt.getOldValue();
			String oldContentType = NamedValue.get("Content-Type", headers);
			headers = (NamedValue[]) evt.getNewValue();
			newContentType = NamedValue.get("Content-Type", headers);
			if (! ObjectUtils.nullSafeEquals(oldContentType, newContentType)) {
				upToDate = false;
				if (isShowing()) {
					showForms(newContentType);
					upToDate = true;
				}
			}
		}

		@Override
		public void componentShown(ComponentEvent e) {
			if (!upToDate) {
				showForms(newContentType);
				upToDate = true;
			}
		}
    	
    }
    
}
