/**
 * 
 */
package org.owasp.webscarab.util;

import java.util.Properties;

import org.springframework.core.closure.Constraint;
import org.springframework.rules.PropertyConstraintProvider;
import org.springframework.rules.Rules;
import org.springframework.rules.constraint.property.PropertyConstraint;

/**
 * @author rdawes
 *
 */
public class JdbcConnectionDetails implements PropertyConstraintProvider {

    public static final String PROPERTY_DRIVERCLASSNAME = "driverClassName";

    public static final String PROPERTY_URL = "url";

    public static final String PROPERTY_USERNAME = "username";

    public static final String PROPERTY_PASSWORD = "password";

    public static final String PROPERTY_PROPERTIES = "properties";

    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private Properties connectionProperties;
    
    private Rules validationRules;

	public JdbcConnectionDetails() {
		initRules();
	}

	public String getDriverClassName() {
		return this.driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public Properties getConnectionProperties() {
		return this.connectionProperties;
	}

	public void setConnectionProperties(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

    /**
     * Initialize the field constraints for our properties. Minimal constraints are
     * enforced here. If you need more control, you should override this in a subtype.
     */
    protected void initRules() {
        this.validationRules = new Rules( getClass() ) {
            protected void initRules() {
                add( PROPERTY_DRIVERCLASSNAME, all( new Constraint[] { required() } ) );
                add( PROPERTY_URL, all( new Constraint[] { required() } ) );
                add( PROPERTY_USERNAME, all( new Constraint[] { required() } ) );
//                add( PROPERTY_PASSWORD, all( new Constraint[] { required() } ) );
            }

        };
    }

    /**
     * Return the property constraints.
     * @see org.springframework.rules.PropertyConstraintProvider#getPropertyConstraint(java.lang.String)
     */
    public PropertyConstraint getPropertyConstraint(String propertyName) {
        return validationRules.getPropertyConstraint( propertyName );
    }

}
