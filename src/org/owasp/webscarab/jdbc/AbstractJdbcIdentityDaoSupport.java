/**
 * 
 */
package org.owasp.webscarab.jdbc;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author rdawes
 *
 */
public abstract class AbstractJdbcIdentityDaoSupport extends JdbcDaoSupport {

    /**
     * Return the identity query for the particular database: a query that can
     * be used to retrieve the id of a row that has just been inserted.
     * 
     * @return the identity query
     */
    protected abstract String getIdentityQuery();

    protected Integer retrieveIdentity() {
        int identity = getJdbcTemplate().queryForInt(getIdentityQuery());
        return new Integer(identity);
    }

}
