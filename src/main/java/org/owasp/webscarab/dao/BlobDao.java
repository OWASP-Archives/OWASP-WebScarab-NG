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

    String saveBlob(byte[] blob);

}
