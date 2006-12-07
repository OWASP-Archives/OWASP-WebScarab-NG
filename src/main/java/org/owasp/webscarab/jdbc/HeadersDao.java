/**
 *
 */
package org.owasp.webscarab.jdbc;

import org.owasp.webscarab.domain.NamedValue;

/**
 * @author rdawes
 *
 */
public interface HeadersDao {

    public final static Integer REQUEST_HEADERS = new Integer(0);
    public final static Integer RESPONSE_HEADERS = new Integer(1);
    public final static Integer RESPONSE_FOOTERS = new Integer(2);

    NamedValue[] findHeaders(Integer conversation, Integer type);

    void saveHeaders(Integer conversation, Integer type, NamedValue[] headers);

}
