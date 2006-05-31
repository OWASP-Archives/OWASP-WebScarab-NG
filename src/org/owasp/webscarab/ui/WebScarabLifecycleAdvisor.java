/**
 * 
 */
package org.owasp.webscarab.ui;

import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationWindowConfigurer;
import org.springframework.richclient.application.config.DefaultApplicationLifecycleAdvisor;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;

/**
 * @author rdawes
 * 
 */
public class WebScarabLifecycleAdvisor extends
		DefaultApplicationLifecycleAdvisor {

	public void onPreWindowOpen(ApplicationWindowConfigurer configurer) {
		super.onPreWindowOpen(configurer);
		// comment out to hide the menubar, toolbar, or reduce window size...
		// configurer.setShowMenuBar(false);
		configurer.setShowToolBar(false);
		// configurer.setInitialSize(new Dimension(640, 480));
		configurer.setShowStatusBar(true);
	}

	@Override
	public void onPostStartup() {
        ApplicationWindowAwareCommand command = (ApplicationWindowAwareCommand) getCommandBarFactory().getBean("selectSessionCommand");
        if (command != null) 
        	command.execute();
	}

	public void onCommandsCreated(ApplicationWindow window) {
		
    }

}
