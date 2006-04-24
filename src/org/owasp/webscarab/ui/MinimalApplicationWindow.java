/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.springframework.richclient.application.config.ApplicationWindowConfigurer;
import org.springframework.richclient.application.support.DefaultApplicationWindow;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.progress.StatusBarCommandGroup;

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

    public Iterator getSharedCommands() {
        return Collections.EMPTY_LIST.iterator();
    }

    protected JMenuBar createMenuBarControl() {
        return null;
    }
    
    protected JComponent createToolBarControl() {
        return null;
    }

    protected JComponent createStatusBarControl() {
        return null;
    }

    public CommandGroup getMenuBar() {
        return null;
    }

    public CommandGroup getToolBar() {
        return null;
    }

    public StatusBarCommandGroup getStatusBar() {
        return null;
    }
    
	
}
