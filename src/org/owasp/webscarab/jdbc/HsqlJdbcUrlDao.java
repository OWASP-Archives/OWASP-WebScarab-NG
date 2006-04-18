/**
 * 
 */
package org.owasp.webscarab.jdbc;

import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author rdawes
 *
 */
public class HsqlJdbcUrlDao extends AbstractJdbcUrlDao {

    /* (non-Javadoc)
     * @see org.owasp.webscarab.jdbc.AbstractJdbcUrlDao#getIdentityQuery()
     */
    @Override
    protected String getIdentityQuery() {
        return "CALL IDENTITY()";
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.jdbc.AbstractJdbcUrlDao#createTables()
     */
    @Override
    protected void createTables() throws SQLException {
        JdbcTemplate template = new JdbcTemplate(this.getDataSource());
        
        logger.fatal("Creating URL tables");
        template.execute(
                "CREATE TABLE urls ( " +
                "id INT NOT NULL IDENTITY PRIMARY KEY, " +
                "url VARCHAR(4096) NOT NULL)");
    }

}
