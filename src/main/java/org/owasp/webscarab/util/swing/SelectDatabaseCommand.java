/**
 *
 */
package org.owasp.webscarab.util.swing;

import java.util.Properties;

import javax.swing.JOptionPane;

import org.bushe.swing.event.EventService;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.jdbc.DataSourceFactory;
import org.owasp.webscarab.util.JdbcConnectionDetails;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.dialog.ApplicationDialog;
import org.springframework.richclient.dialog.CompositeDialogPage;
import org.springframework.richclient.dialog.TabbedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.FormModelHelper;

/**
 * Provides an interface for selecting a database to use.
 * <P>
 * Presents a dialog to the user to collect JDBC connect information. It then
 * opens the connection to the database, and configures the connection using the
 * provided bean identifier
 * <p>
 * If the connection is unsuccesful, a message is presented to the user and they
 * are offered another chance to select a database.
 * <p>
 * The <code>closeOnCancel</code> property controls what happens if the user
 * cancels the selection dialog. If closeOnCancel is true (the default), if
 * there is no valid database in place (from a previous selection) then the
 * application is closed. If it is false or a database is available, then no
 * action is taken other than closing the dialog.
 * <p>
 * A typical configuration for this component might look like this:
 *
 * <pre>
 *           &lt;bean id=&quot;placeholderConfigurer&quot;
 *                class=&quot;org.springframework.beans.factory.config.PropertiesPlaceholderConfigurer&quot;/&gt;
 *
 *            &lt;bean id=&quot;selectDatabaseCommand&quot;
 *                class=&quot;org.owasp.webscarab.util.swing.SelectDatabaseCommand&quot;&gt;
 *                &lt;property name=&quot;displaySuccess&quot; value=&quot;false&quot;/&gt;
 *                &lt;property name=&quot;defaultUserName&quot; value=&quot;${user.name}&quot;/&gt;
 *            &lt;/bean&gt;
 * </pre>
 *
 * @author Ben Alex
 * @author Larry Streepy
 *
 * @see LoginForm
 * @see LoginDetails
 * @see ApplicationSecurityManager
 */
public class SelectDatabaseCommand extends ApplicationWindowAwareCommand {

	private static final String ID = "selectDatabaseCommand";

	private boolean displaySuccessMessage = true;

	private boolean closeOnCancel = true;

	private ApplicationDialog dialog = null;

	private DataSourceFactory dataSourceFactory = null;

	private EventService eventService = null;

	/**
	 * Constructor.
	 */
	public SelectDatabaseCommand() {
		super(ID);
	}

	/**
	 * Indicates whether an information message is displayed to the user upon
	 * successful authentication. Defaults to true.
	 *
	 * @param displaySuccessMessage
	 *            displays an information message upon successful login if true,
	 *            otherwise false
	 */
	public void setDisplaySuccessMessage(boolean displaySuccessMessage) {
		this.displaySuccessMessage = displaySuccessMessage;
	}

	/**
	 * Execute the login command. Display the dialog and attempt authentication.
	 */
    protected void doExecuteCommand() {
		CompositeDialogPage tabbedPage = new TabbedDialogPage(
				"selectDatabaseForm");

		final JdbcDetailsForm jdbcDetailsForm = createJdbcDetailsForm();

		tabbedPage.addForm(jdbcDetailsForm);

		dialog = new TitledPageApplicationDialog(tabbedPage) {
			protected boolean onFinish() {
				jdbcDetailsForm.commit();

				JdbcConnectionDetails jdbcDetails = (JdbcConnectionDetails) jdbcDetailsForm
						.getFormObject();

				try {
					getDataSourceFactory().setJdbcConnectionDetails(jdbcDetails);
					postSelection();
				} catch (Exception e) {
					logger.error("Error opening connection", e);
					// Any exception here means that the connection failed
					// Report it and return false
					boolean rtn = handleConnectionFailure(e);
					jdbcDetailsForm.requestFocusInWindow();
					return rtn;
				}
				return true;
			}

			protected void onCancel() {
				super.onCancel(); // Close the dialog

				// Now exit if configured
				if (isCloseOnCancel()) {
					logger
							.info("User canceled selection; close the application.");
					getApplication().close();
				}
			}

			protected ActionCommand getCallingCommand() {
				return SelectDatabaseCommand.this;
			}

			protected void onAboutToShow() {
				jdbcDetailsForm.requestFocusInWindow();
			}
		};
		dialog.setDisplayFinishSuccessMessage(displaySuccessMessage);
		dialog.showDialog();
	}

	/**
	 * Construct the Form to place in the dialog.
	 *
	 * @return form to use
	 */
	protected JdbcDetailsForm createJdbcDetailsForm() {
		JdbcConnectionDetails jcd = new JdbcConnectionDetails();
    	jcd.setDriverClassName("org.hsqldb.jdbcDriver");
    	jcd.setUrl("jdbc:hsqldb:file:c:/temp/webscarab/;hsqldb.default_table_type=cached");
    	jcd.setUsername("sa");
        Properties properties = new Properties();
        properties.setProperty("hsqldb.default_table_type", "cached");
        properties.setProperty("default_table_type", "cached");
        jcd.setConnectionProperties(properties);
		ValidatingFormModel model = FormModelHelper.createUnbufferedFormModel(jcd);
		return new JdbcDetailsForm(model);
	}

	/**
	 * Get the dialog in use, if available.
	 *
	 * @return dialog instance in use
	 */
	protected ApplicationDialog getDialog() {
		return dialog;
	}

	/**
	 * Called to give subclasses control after a successful selection.
	 */
	protected void postSelection() {
		if (getEventService() != null) {
            Session session = new Session();
            session.setId(0);
			eventService.publish(new SessionEvent(this, session));
        }
	}

	/**
	 * Report a login failure. Base implementation just displays a message
	 * dialog with the localized message from the security exception.
	 *
	 * @param authentication
	 *            token that failed to authenticate
	 * @param exception
	 *            The exception indicating the authentication failure
	 * @return true if the login dialog should be closed, base implementation
	 *         always returns false
	 */
	protected boolean handleConnectionFailure(Exception exception) {
		String exceptionMessage = exception.getLocalizedMessage();
		JOptionPane.showMessageDialog(getDialog().getDialog(),
				exceptionMessage, Application.instance().getName(),
				JOptionPane.ERROR_MESSAGE);

		return false;
	}

	/**
	 * Get the "close on cancel" setting.
	 *
	 * @return close on cancel
	 */
	public boolean isCloseOnCancel() {
		return closeOnCancel;
	}

	/**
	 * Indicates if the application should be closed if the user cancels the
	 * selection operation. Default is true.
	 *
	 * @param closeOnCancel
	 */
	public void setCloseOnCancel(boolean closeOnCancel) {
		this.closeOnCancel = closeOnCancel;
	}

	public DataSourceFactory getDataSourceFactory() {
		return this.dataSourceFactory;
	}

	public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
		this.dataSourceFactory = dataSourceFactory;
	}

	public EventService getEventService() {
		return this.eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

}