/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Conversation;

/**
 * @author rdawes
 *
 */
public class ConversationSelectionEvent implements EventServiceEvent {

    private Object source;

    private Conversation[] selection;

    public ConversationSelectionEvent(Object source, Conversation[] selection) {
        this.source = source;
        this.selection = selection;
    }

    /* (non-Javadoc)
     * @see org.bushe.swing.event.EventServiceEvent#getSource()
     */
    public Object getSource() {
        return this.source;
    }

    public Conversation[] getSelection() {
        return this.selection;
    }
}
