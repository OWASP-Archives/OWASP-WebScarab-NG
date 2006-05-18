/**
 * 
 */
package org.owasp.webscarab.jdbc;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.owasp.webscarab.util.JdbcConnectionDetails;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author rdawes
 *
 */
public class DataSourceFactory implements FactoryBean {

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
	
}
