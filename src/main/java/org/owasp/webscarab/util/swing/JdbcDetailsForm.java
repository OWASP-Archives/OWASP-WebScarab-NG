/**
 *
 */
package org.owasp.webscarab.util.swing;

import javax.swing.JComponent;

import org.owasp.webscarab.util.JdbcConnectionDetails;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * This class provides a simple form for capturing a JDBC connection string from the user.
 * It also generates an {@link JdbcConnectionDetails} object from the entered values.
 *
 * @author rdawes
 * @author Larry Streepy
 * @see #getJdbcConnectionDetails()
 *
 */
public class JdbcDetailsForm extends AbstractForm {

    private static final String FORM_ID = "jdbcConnection";

	private JdbcConnectionDetails jdbcDetails;

	private JComponent classNameField;
	private JComponent urlField;
    private JComponent usernameField;
    private JComponent passwordField;

    /**
     * Constructor.
     */
    public JdbcDetailsForm(FormModel model) {
    	super(model, FORM_ID);
    	jdbcDetails = (JdbcConnectionDetails) model.getFormObject();
    }

    /**
     * Construct the form with the required fields.
     */
    protected JComponent createFormControl() {
        TableFormBuilder formBuilder = new TableFormBuilder( getBindingFactory() );
        classNameField = formBuilder.add( JdbcConnectionDetails.PROPERTY_DRIVERCLASSNAME)[1];
        formBuilder.row();
        urlField = formBuilder.add( JdbcConnectionDetails.PROPERTY_URL)[1];
        formBuilder.row();
        usernameField = formBuilder.add( JdbcConnectionDetails.PROPERTY_USERNAME)[1];
        formBuilder.row();
        passwordField = formBuilder.addPasswordField( JdbcConnectionDetails.PROPERTY_PASSWORD )[1];
        return formBuilder.getForm();
    }

    public boolean requestFocusInWindow() {
        // Put the focus on the right field
    	String driverClassName = jdbcDetails.getDriverClassName();
    	String url = jdbcDetails.getUrl();
        String username = jdbcDetails.getUsername();
        JComponent field = null;
        if (driverClassName == null || driverClassName.length() == 0) {
        	field = classNameField;
        } else if (url == null || url.length() == 0) {
        	field = urlField;
        } else if (username == null || username.length() == 0) {
        	field = usernameField;
        } else {
        	field = passwordField;
        }
        return field.requestFocusInWindow();
    }

}

