/**
 *
 */
package org.owasp.webscarab.util.swing;

import java.awt.Dimension;

import org.springframework.richclient.application.docking.vldocking.VLDockingLayoutManager;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * @author rdawes
 *
 */
public class WSLayoutManager implements VLDockingLayoutManager {

    /* (non-Javadoc)
     * @see org.springframework.richclient.application.vldocking.VLDockingLayoutManager#addDockable(com.vlsolutions.swing.docking.DockingDesktop, com.vlsolutions.swing.docking.Dockable)
     */
    public void addDockable(DockingDesktop desktop, Dockable dockable) {
        System.out.println("Adding " + dockable.getDockKey().getName());
        // find the largest place to put the new item.
        DockableState[] states = desktop.getDockables();
        if (states == null || states.length == 0) {
            desktop.addDockable(dockable);
            return;
        }
        for (int i=0; i<states.length; i++) {
            if (states[i].isMaximized())
                desktop.restore(states[i].getDockable());
        }
        // refresh since we may have changed states of items.
        states = desktop.getDockables();
        Dockable target = null;
        for (int i=0; i<states.length; i++) {
            if (target == null || compare(target.getComponent().getSize(), states[i].getDockable().getComponent().getSize()) < 0) {
                target = states[i].getDockable();
            }
        }
        desktop.createTab(target, dockable, Integer.MAX_VALUE, true);
        // desktop.addDockable(dockable);
    }

    private int compare(Dimension d1, Dimension d2) {
        System.out.println("Comparing " + d1 + " and " + d2);
        double a1 = d1.height * d1.width;
        double a2 = d2.height * d2.width;
        return Double.compare(a1, a2);
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.application.vldocking.VLDockingLayoutManager#removeDockable(com.vlsolutions.swing.docking.DockingDesktop, com.vlsolutions.swing.docking.Dockable)
     */
    public void removeDockable(DockingDesktop desktop, Dockable dockable) {
        desktop.remove(dockable);
    }

}
