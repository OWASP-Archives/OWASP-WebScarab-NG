/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.Dimension;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import org.bushe.swing.event.EventService;
import org.owasp.webscarab.WebScarab;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.jdbc.DataSourceFactory;
import org.owasp.webscarab.util.JdbcConnectionDetails;
import org.owasp.webscarab.util.rcp.ScreenAwareWindowMemento;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationWindowConfigurer;
import org.springframework.richclient.application.config.DefaultApplicationLifecycleAdvisor;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.settings.Settings;
import org.springframework.richclient.settings.j2seprefs.PreferencesSettings;
import org.springframework.richclient.settings.support.WindowMemento;

/**
 * @author rdawes
 *
 */
public class WebScarabLifecycleAdvisor extends
		DefaultApplicationLifecycleAdvisor {

    private DataSourceFactory dataSourceFactory;
    
    private EventService eventService;
    
	public void onPreWindowOpen(ApplicationWindowConfigurer configurer) {
		super.onPreWindowOpen(configurer);
		// comment out to hide the menubar, toolbar, or reduce window size...
		// configurer.setShowMenuBar(false);
		configurer.setShowToolBar(true);
        configurer.setInitialSize(new Dimension(970, 700));
		configurer.setShowStatusBar(true);
	}

    /* (non-Javadoc)
     * @see org.springframework.richclient.application.config.ApplicationLifecycleAdvisor#onWindowCreated(org.springframework.richclient.application.ApplicationWindow)
     */
    @Override
    public void onWindowCreated(ApplicationWindow window) {
        WindowMemento wm = new ScreenAwareWindowMemento(window.getControl());
        wm.restoreState(getSettings());
        super.onWindowCreated(window);
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.application.config.ApplicationLifecycleAdvisor#onPreWindowClose(org.springframework.richclient.application.ApplicationWindow)
     */
    @Override
    public boolean onPreWindowClose(ApplicationWindow window) {
        boolean ret = super.onPreWindowClose(window);
        if (ret) {
            WindowMemento wm = new WindowMemento(window.getControl());
            wm.saveState(getSettings());
        }
        return ret;
    }

	private Settings getSettings() {
	    return new PreferencesSettings(Preferences.userRoot().node("org/owasp/webscarab/ui/rcp"));
	}

	/* (non-Javadoc)
	 * @see org.springframework.richclient.application.config.ApplicationLifecycleAdvisor#onPreStartup()
	 */
	@Override
	public void onPostStartup() {
	    if (WebScarab.args != null) {
	        if (WebScarab.args.length == 1) {
    	        JdbcConnectionDetails jdbc = new JdbcConnectionDetails();
    	        jdbc.setDriverClassName("org.hsqldb.jdbcDriver");
    	        jdbc.setUrl("jdbc:hsqldb:file:" + WebScarab.args[0]);
    	        jdbc.setUsername("sa");
    	        jdbc.setPassword(null);
                try {
                    // do an initial test creation of the datasource
                    getDataSourceFactory().createDataSource(jdbc, true);
                    getDataSourceFactory().setJdbcConnectionDetails(jdbc);
                    if (getEventService() != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                Session session = new Session();
                                session.setId(0);
                                getEventService().publish(new SessionEvent(this, session));
                            }
                        });
                        return;
                    }
                } catch (Exception e) {
                    // error
                }
	        } else {
	            // error
	        }
	    }
        final ApplicationWindowAwareCommand command = (ApplicationWindowAwareCommand) getCommandBarFactory().getBean("selectSessionCommand");
        if (command != null)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    command.execute();
                }
            });
	}

	public void setDataSourceFactory(DataSourceFactory factory) {
	    this.dataSourceFactory = factory;
	}
	
	private DataSourceFactory getDataSourceFactory() {
	    return dataSourceFactory;
	}
	
	public void setEventService(EventService eventService) {
	    this.eventService = eventService;
	}
	
	private EventService getEventService() {
	    return eventService;
	}

}
