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
    
    public SessionEvent(Object source) {
    	super(source);
        this.type = SESSION_CHANGED;
    }
    
    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }

}
