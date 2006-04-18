/**
 * 
 */
package org.owasp.webscarab.dao;

import java.net.URL;

/**
 * @author rdawes
 *
 */
public interface UrlDao {

    Integer findUrlId(URL url);
    
    URL findUrl(Integer id);
    
    Integer saveUrl(URL url);
    
}
