/**
 * 
 */
package org.owasp.webscarab.ui;

import javax.swing.JComponent;

import org.springframework.richclient.application.support.AbstractView;

/**
 * @author rdawes
 *
 */
public class LogoView extends AbstractView {

	@Override
	protected JComponent createControl() {
		return getComponentFactory().createLabel("LogoView");
	}

}
