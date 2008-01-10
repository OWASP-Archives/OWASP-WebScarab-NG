/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.Dimension;

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
		configurer.setShowToolBar(true);
        configurer.setInitialSize(new Dimension(970, 700));
		configurer.setShowStatusBar(true);
	}

	/* (non-Javadoc)
	 * @see org.springframework.richclient.application.config.ApplicationLifecycleAdvisor#onPreStartup()
	 */
	@Override
	public void onPostStartup() {
        ApplicationWindowAwareCommand command = (ApplicationWindowAwareCommand) getCommandBarFactory().getBean("selectSessionCommand");
        if (command != null)
        	command.execute();
	}

}
