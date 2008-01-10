/**
 * 
 */
package org.owasp.webscarab.ui.rcp;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.springframework.richclient.application.support.AbstractView;

/**
 * @author rdawes
 *
 */
public class LogoView extends AbstractView {

	@Override
	protected JComponent createControl() {
		return new JLabel(getIconSource().getIcon("applicationInfo.image"));
	}

}
