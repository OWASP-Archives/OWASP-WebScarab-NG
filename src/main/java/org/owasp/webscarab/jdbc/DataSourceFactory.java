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

	private DataSource dataSource = null;
	
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

	public void setJdbcConnectionDetails(JdbcConnectionDetails jdbcConnectionDetails) throws SQLException {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(jdbcConnectionDetails.getDriverClassName());
		dataSource.setUrl(jdbcConnectionDetails.getUrl());
		dataSource.setUsername(jdbcConnectionDetails.getUsername());
		dataSource.setPassword(jdbcConnectionDetails.getPassword());
		dataSource.getConnection().close();
		this.dataSource = dataSource;
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
