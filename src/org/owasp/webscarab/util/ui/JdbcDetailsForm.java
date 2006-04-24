/**
 * 
 */
package org.owasp.webscarab.util.ui;

import javax.swing.JComponent;

import org.owasp.webscarab.util.JdbcConnectionDetails;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.FormModelHelper;
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
    public JdbcDetailsForm() {
        super( FORM_ID );

        jdbcDetails = createJdbcConnectionDetails();
        setFormModel( FormModelHelper.createUnbufferedFormModel( jdbcDetails ) );
    }

    /**
     * Set the user name in the form.
     * @param userName to install
     */
    public void setDriverClassName(String driverClassName) {
        if( isControlCreated() ) {
            getValueModel( JdbcConnectionDetails.PROPERTY_DRIVERCLASSNAME).setValue( driverClassName );
        } else {
            jdbcDetails.setDriverClassName( driverClassName );
        }
    }

    /**
     * Set the user name in the form.
     * @param userName to install
     */
    public void setUrl(String url) {
        if( isControlCreated() ) {
            getValueModel( JdbcConnectionDetails.PROPERTY_URL).setValue( url );
        } else {
            jdbcDetails.setUrl( url );
        }
    }

    /**
     * Set the user name in the form.
     * @param userName to install
     */
    public void setUserName(String userName) {
        if( isControlCreated() ) {
            getValueModel( JdbcConnectionDetails.PROPERTY_USERNAME ).setValue( userName );
        } else {
            jdbcDetails.setUsername( userName );
        }
    }

    /**
     * Set the password in the form.
     * @param password to install
     */
    public void setPassword(String password) {
        if( isControlCreated() ) {
            getValueModel( JdbcConnectionDetails.PROPERTY_PASSWORD ).setValue( password );
        } else {
            jdbcDetails.setPassword( password );
        }
    }

    public JdbcConnectionDetails getJdbcConnectionDetails() {
    	return jdbcDetails;
    }
    
    /**
     * Create the form object to hold our JDBC information.
     * @return constructed form object
     */
    protected JdbcConnectionDetails createJdbcConnectionDetails() {
    	JdbcConnectionDetails jcd = new JdbcConnectionDetails();
    	jcd.setDriverClassName("org.hsqldb.jdbcDriver");
    	jcd.setUrl("jdbc:hsqldb:file:c:/temp/webscarab/");
    	jcd.setUsername("sa");
    	return jcd;
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

