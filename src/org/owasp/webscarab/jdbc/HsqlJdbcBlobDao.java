/**
 * 
 */
package org.owasp.webscarab.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author rdawes
 * 
 */
public class HsqlJdbcBlobDao extends AbstractJdbcBlobDao {

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.jdbc.AbstractJdbcBlobDao#createTables()
     */
    @Override
    protected void createTables() {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        template.execute("CREATE TABLE blobs ("
                + "key CHAR(32) NOT NULL PRIMARY KEY," + "blob LONGVARBINARY)");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.jdbc.AbstractJdbcDaoSupport#getIdentityQuery()
     */
    @Override
    protected String getIdentityQuery() {
        return "CALL IDENTITY()";
    }

}
