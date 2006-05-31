/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.springframework.richclient.application.config.ApplicationWindowConfigurer;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.support.DialogPageUtils;

/**
 * @author rdawes
 *
 */
public class ConversationFrame extends JFrame {

	private static final long serialVersionUID = -6741587265840954245L;

	public ConversationFrame(DialogPage page, ActionCommand okCommand) {
		getContentPane().setLayout(new BorderLayout());
		JComponent component = DialogPageUtils.createStandardView(page, okCommand, null);
		getContentPane().add(component, BorderLayout.CENTER);
		if (page.getImage() != null)
			setIconImage(page.getImage());
		if (page.getTitle() != null)
			setTitle(page.getTitle());
	}
	
	public void configure(ApplicationWindowConfigurer configurer) {
		if (configurer.getInitialSize() != null) 
			setSize(configurer.getInitialSize());
//		if (configurer.getTitle() != null)
//			setTitle(configurer.getTitle());
//		if (configurer.getImage() != null)
//			setIconImage(configurer.getImage());
	}
	
}
