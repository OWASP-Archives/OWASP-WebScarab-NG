/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.springframework.richclient.application.config.ApplicationWindowConfigurer;
import org.springframework.richclient.application.support.DefaultApplicationWindow;

/**
 * @author rdawes
 *
 */
public class MinimalApplicationWindow extends DefaultApplicationWindow {

	public MinimalApplicationWindow() {
		super();
	}
	
    protected void applyStandardLayout(JFrame windowControl, ApplicationWindowConfigurer configurer) {
        windowControl.setTitle(configurer.getTitle());
        windowControl.setIconImage(configurer.getImage());
//        windowControl.setJMenuBar(createMenuBarControl());
        windowControl.getContentPane().setLayout(new BorderLayout());
//        windowControl.getContentPane().add(createToolBarControl(), BorderLayout.NORTH);
//        windowControl.getContentPane().add(createStatusBarControl(), BorderLayout.SOUTH);
    }

	
}
