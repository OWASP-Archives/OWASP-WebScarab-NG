/**
 * 
 */
package org.owasp.webscarab.ui.rcp;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.bushe.swing.event.EventServiceEvent;
import org.bushe.swing.event.EventSubscriber;
import org.bushe.swing.event.EventTopicSubscriber;

/**
 * @author rdawes
 *
 */
public class SwingEventSubscriber implements EventSubscriber, EventTopicSubscriber {

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
    
	public void onEvent(final String topic, final Object data) {
        if (SwingUtilities.isEventDispatchThread()) {
            handleEventOnEDT(topic, data);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        handleEventOnEDT(topic, data);
                    }
                });
            } catch (InvocationTargetException ite) {
                ite.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
	}

    protected void handleEventOnEDT(EventServiceEvent evt) {}

    protected void handleEventOnEDT(String topic, Object data) {}

}
