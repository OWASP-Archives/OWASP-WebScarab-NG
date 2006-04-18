/**
 * 
 */
package org.owasp.webscarab.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author rdawes
 *
 */
public class HsqlJdbcHeadersDao extends AbstractJdbcHeadersDao {

    public HsqlJdbcHeadersDao(NamedValueDao namedValueDao) {
        super(namedValueDao);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.jdbc.AbstractJdbcIdentityDaoSupport#getIdentityQuery()
     */
    @Override
    protected String getIdentityQuery() {
        return "CALL IDENTITY()";
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.jdbc.AbstractJdbcHeadersDao#createTables()
     */
    @Override
    public void createTables() {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        
        template.execute(
                "CREATE CACHED TABLE headers (" +
                "sort INT NOT NULL IDENTITY," +
                "conversation INT NOT NULL," +
                "type INT NOT NULL," +
                "named_value INT NOT NULL)");
    }
    
    
}
