/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.net.URI;

import org.bushe.swing.event.EventServiceEvent;

/**
 * @author rdawes
 *
 */
public class URISelectionEvent implements EventServiceEvent {

    private Object source;
    private URI[] selection;

    public URISelectionEvent(Object source, URI[] selection) {
        this.source = source;
        this.selection = selection;
    }
    /* (non-Javadoc)
     * @see org.bushe.swing.event.EventServiceEvent#getSource()
     */
    public Object getSource() {
        return source;
    }

    public URI[] getSelection() {
        return this.selection;
    }
}
