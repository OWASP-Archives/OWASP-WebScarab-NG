/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author rdawes
 *
 */
public abstract class PropertiesJdbcDaoSupport extends JdbcDaoSupport {

	private Properties properties;
	private String identityQuery = null;

	public PropertiesJdbcDaoSupport() {}

    public Properties getProperties() {
		return this.properties;
	}

    public void setProperties(Properties properties) {
		this.properties = properties;
	}

    protected String getSubprotocolName() {
    	Connection connection = null;
    	try {
    		connection = getDataSource().getConnection();
    		String url = connection.getMetaData().getURL();
    		String[] part = url.split(":", 3);
    		return part[1];
    	} catch (SQLException se) {
    		return null;
    	} finally {
    		if (connection != null) {
    			try {
    				connection.close();
    			} catch (SQLException se2) {}
    		}
    	}
    }

    protected String getProperty(String name) {
    	String product = getSubprotocolName();
    	String value = properties.getProperty(name + "." + product);
    	if (value != null) return value;
    	value = properties.getProperty(name);
    	if (value != null) return value;
    	throw new DataRetrievalFailureException("No property found for " + name + " for product " + product);
    }

    protected String getIdentityQuery() {
    	if (identityQuery == null)
    		identityQuery = getProperty("identityQuery");
    	return identityQuery;
    }

    protected Integer retrieveIdentity() {
        int identity = getJdbcTemplate().queryForInt(getIdentityQuery());
        return new Integer(identity);
    }

}
