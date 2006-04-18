/**
 * 
 */
package org.owasp.webscarab.dao;

/**
 * @author rdawes
 *
 */
public interface BlobDao {

    boolean exists(String key);
    
    byte[] findBlob(String key);
    
    void saveBlob(String key, byte[] blob);
    
}
