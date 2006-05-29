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
public class WebScarabLifecycleAdvisor extends DefaultApplicationLifecycleAdvisor {

	private ApplicationWindowAwareCommand introCommand = null;
	
	public void onPreWindowOpen(ApplicationWindowConfigurer configurer) {
        super.onPreWindowOpen(configurer);
        // comment out to hide the menubar, toolbar, or reduce window size...
//        configurer.setShowMenuBar(false);
        configurer.setShowToolBar(false);
//        configurer.setInitialSize(new Dimension(640, 480));
    }

	@Override
	protected void showIntro(ApplicationWindow applicationWindow) {
		super.showIntro(applicationWindow);
		if (introCommand != null) {
			introCommand.setApplicationWindow(applicationWindow);
			introCommand.execute();
		}
	}

	public ApplicationWindowAwareCommand getIntroCommand() {
		return this.introCommand;
	}

	public void setIntroCommand(
			ApplicationWindowAwareCommand postStartupCommand) {
		this.introCommand = postStartupCommand;
	}

}
