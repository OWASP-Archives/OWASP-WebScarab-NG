/**
 *
 */
package org.owasp.webscarab.domain;

import org.bushe.swing.event.AbstractEventServiceEvent;

/**
 * @author rdawes
 *
 */
public class SessionEvent extends AbstractEventServiceEvent {

    public final static int SESSION_CHANGED = 0;

    private int type;
    private Session session;

    public SessionEvent(Object source, Session session) {
    	super(source);
        this.type = SESSION_CHANGED;
        this.session = session;
    }

    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }

    /**
     * @return Returns the session
     */
    public Session getSession() {
        return this.session;
    }

}
