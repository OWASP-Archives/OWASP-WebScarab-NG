/**
 *
 */
package org.owasp.webscarab.dao;

/**
 * @author rdawes
 *
 */
public interface ConversationSourceDao {

    String getSource(Integer source);

    Integer addSource(String source);

}
