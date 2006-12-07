/**
 *
 */
package org.owasp.webscarab.jdbc;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author rdawes
 *
 */
public class VersionDao {

    public static final Integer VERSION_0 = new Integer(0);
    public static final Integer VERSION_1 = new Integer(1);

    public Integer getId(String version) {
        if ("HTTP/1.0".equals(version)) return VERSION_0;
        if ("HTTP/1.1".equals(version)) return VERSION_1;
        throw new DataRetrievalFailureException("Unknown HTTP version: " + version);
    }

    public String getVersion(Integer id) {
        if (VERSION_0.equals(id)) return "HTTP/1.0";
        if (VERSION_1.equals(id)) return "HTTP/1.1";
        throw new DataRetrievalFailureException("Unknown HTTP version id: " + id);
    }

}
