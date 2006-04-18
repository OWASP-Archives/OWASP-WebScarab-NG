/**
 * 
 */
package org.owasp.webscarab.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author rdawes
 *
 */
public class HsqlJdbcNamedValueDao extends AbstractJdbcNamedValueDao {

    /* (non-Javadoc)
     * @see org.owasp.webscarab.jdbc.AbstractJdbcNamedValueDao#createTables()
     */
    @Override
    protected void createTables() {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        
        template.execute(
                "CREATE CACHED TABLE named_values (" +
                "id INT NOT NULL IDENTITY," +
                "name VARCHAR(32) NOT NULL," +
                "value VARCHAR(1024) NOT NULL)");
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.jdbc.AbstractJdbcIdentityDaoSupport#getIdentityQuery()
     */
    @Override
    protected String getIdentityQuery() {
        return "CALL IDENTITY()";
    }

}
