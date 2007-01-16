/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.owasp.webscarab.util.JdbcConnectionDetails;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author rdawes
 *
 */
public class DataSourceFactory implements FactoryBean, DisposableBean {

	private DriverManagerDataSource dataSource = null;

    private JdbcConnectionDetails jdbcConnectionDetails = null;

	public Class getObjectType() {
		return DataSource.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		if (dataSource == null)
			throw new NullPointerException("DataSource has not yet been created");
		return dataSource;
	}

    /**
     * @return the jdbcConnectionDetails
     */
    public JdbcConnectionDetails getJdbcConnectionDetails() {
        return this.jdbcConnectionDetails;
    }

	public void setJdbcConnectionDetails(JdbcConnectionDetails jdbcConnectionDetails) throws SQLException {
		try {
            this.jdbcConnectionDetails = jdbcConnectionDetails;
            dataSource = new DriverManagerDataSource();
    		dataSource.setDriverClassName(jdbcConnectionDetails.getDriverClassName());
    		dataSource.setUrl(jdbcConnectionDetails.getUrl());
    		dataSource.setUsername(jdbcConnectionDetails.getUsername());
    		dataSource.setPassword(jdbcConnectionDetails.getPassword());
            dataSource.setConnectionProperties(jdbcConnectionDetails.getConnectionProperties());
    		dataSource.getConnection().close();
        } catch (SQLException se) {
            this.dataSource = null;
            throw se;
        }
	}

	public void destroy() throws Exception {
		if (dataSource != null) {
			// FIXME: This is a bit of a hack still. We should probably only
			// do this if the Driver IS actually HSQLDB. For the moment, it is Ok
			JdbcTemplate jt = new JdbcTemplate(dataSource);
			jt.execute("SHUTDOWN");
		}
	}

}
