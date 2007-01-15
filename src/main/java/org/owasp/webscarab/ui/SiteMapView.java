/**
 *
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.util.swing.UriTreeModel;
import org.owasp.webscarab.util.swing.renderers.UriRenderer;
import org.springframework.richclient.application.support.AbstractView;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * @author rdawes
 *
 */
public class SiteMapView extends AbstractView {

    private ArrayList<URI> uris;

    private EventList<Conversation> conversationList;

    private Listener listener = new Listener();

    private UriTreeModel uriTreeModel;

    public SiteMapView() {
        uris = new ArrayList<URI>();
        uriTreeModel = new UriTreeModel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.richclient.application.support.AbstractView#createControl()
     */
    @Override
    protected JComponent createControl() {
        JPanel panel = getComponentFactory().createPanel(new BorderLayout());
        JTree uriTree = new JTree(uriTreeModel);
        uriTree.setRootVisible(false);
        uriTree.setShowsRootHandles(true);
        uriTree.setCellRenderer(new UriRenderer());
        JScrollPane scrollPane = getComponentFactory().createScrollPane(
                uriTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * @param conversationList
     *            the conversationList to set
     */
    public void setConversationList(EventList<Conversation> conversationList) {
        if (this.conversationList != null)
            this.conversationList.removeListEventListener(listener);
        this.conversationList = conversationList;
        if (conversationList != null) {
            populateExisting();
            conversationList.addListEventListener(listener);
        }
    }

    private void populateExisting() {
        conversationList.getReadWriteLock().readLock().lock();
        uriTreeModel.clear();
        uris.clear();
        Iterator<Conversation> it = conversationList.iterator();
        while (it.hasNext()) {
            URI uri = it.next().getRequestUri();
            uris.add(uri);
            uriTreeModel.add(uri);
        }
        conversationList.getReadWriteLock().readLock().unlock();
    }

    private class Listener implements ListEventListener<Conversation> {

        /*
         * (non-Javadoc)
         *
         * @see ca.odell.glazedlists.event.ListEventListener#listChanged(ca.odell.glazedlists.event.ListEvent)
         */
        public void listChanged(ListEvent<Conversation> evt) {
            while (evt.next()) {
                int index = evt.getIndex();
                if (evt.getType() == ListEvent.DELETE) {
                    uriTreeModel.remove(uris.remove(index));
                } else if (evt.getType() == ListEvent.INSERT) {
                    URI uri = conversationList.get(index).getRequestUri();
                    uris.add(index, uri);
                    uriTreeModel.add(uri);
                }
            }
        }

    }

}
