/**
 *
 */
package org.owasp.webscarab.dao;

import java.util.Collection;

import org.owasp.webscarab.domain.Session;

/**
 * @author rdawes
 *
 */
public interface SessionDao {

	Collection<Session> getSessions();

	Session createSession(String name);

    void updateSession(Session session);

}
