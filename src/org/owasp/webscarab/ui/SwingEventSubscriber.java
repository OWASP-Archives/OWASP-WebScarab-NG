/**
 * 
 */
package org.owasp.webscarab.ui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.bushe.swing.event.EventServiceEvent;
import org.bushe.swing.event.EventSubscriber;

/**
 * @author rdawes
 *
 */
public abstract class SwingEventSubscriber implements EventSubscriber {

    /* (non-Javadoc)
     * @see org.bushe.swing.event.EventSubscriber#onEvent(org.bushe.swing.event.EventServiceEvent)
     */
    public void onEvent(final EventServiceEvent evt) {
        if (SwingUtilities.isEventDispatchThread()) {
            handleEventOnEDT(evt);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        handleEventOnEDT(evt);
                    }
                });
            } catch (InvocationTargetException ite) {
                ite.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
    
    protected abstract void handleEventOnEDT(EventServiceEvent evt);

}
