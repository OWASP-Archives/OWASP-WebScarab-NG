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

    private JdbcConnectionDetails jdbcConnectionDetails = null;

	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return DataSource.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		if (dataSource == null)
            dataSource = createDataSource(jdbcConnectionDetails, false);
		return dataSource;
	}

    public DataSource createDataSource(JdbcConnectionDetails jdbcConnectionDetails, boolean test) throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(jdbcConnectionDetails.getDriverClassName());
        dataSource.setUrl(jdbcConnectionDetails.getUrl());
        dataSource.setUsername(jdbcConnectionDetails.getUsername());
        dataSource.setPassword(jdbcConnectionDetails.getPassword());
        dataSource.setConnectionProperties(jdbcConnectionDetails.getConnectionProperties());
        if (test) {
            // open a connection to make sure we can, then close it again
            dataSource.getConnection().close();
            // if it is a local HSQLDB, make sure we clean up after us
            if (isLocalHsqldb(jdbcConnectionDetails)) {
                shutdownLocalHsqldb(dataSource);
            }
            return null;
        }
        return dataSource;
    }

    private boolean isLocalHsqldb(JdbcConnectionDetails jcd) {
        return jcd.getDriverClassName().equals("org.hsqldb.jdbcDriver") && jcd.getUrl().startsWith("jdbc:hsqldb:file:");
    }

    private void shutdownLocalHsqldb(DataSource dataSource) {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        jt.execute("SHUTDOWN");
    }

    /**
     * @return the jdbcConnectionDetails
     */
    public JdbcConnectionDetails getJdbcConnectionDetails() {
        return this.jdbcConnectionDetails;
    }

	public void setJdbcConnectionDetails(JdbcConnectionDetails jdbcConnectionDetails) {
        this.jdbcConnectionDetails = jdbcConnectionDetails;
	}

	public void destroy() throws Exception {
		if (dataSource != null && isLocalHsqldb(jdbcConnectionDetails)) {
            shutdownLocalHsqldb(dataSource);
		}
	}

}
