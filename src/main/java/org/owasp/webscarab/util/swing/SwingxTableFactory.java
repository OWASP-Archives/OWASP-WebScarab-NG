/**
 * 
 */
package org.owasp.webscarab.util.swing;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.springframework.richclient.factory.TableFactory;

/**
 * @author rdawes
 *
 */
public class SwingxTableFactory implements TableFactory {

	/* (non-Javadoc)
	 * @see org.springframework.richclient.factory.TableFactory#createTable()
	 */
	public JTable createTable() {
		JXTable table = new JXTable() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			// overridden to make the height of scroll match viewport height if smaller
            public boolean getScrollableTracksViewportHeight() {
                return getPreferredSize().height < getParent().getHeight();
            }
            // overridden to make the width of scroll match viewport width if smaller
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
		};
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		return table;
	}

	/* (non-Javadoc)
	 * @see org.springframework.richclient.factory.TableFactory#createTable(javax.swing.table.TableModel)
	 */
	public JTable createTable(TableModel model) {
        JXTable table = new JXTable(model) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			// overridden to make the height of scroll match viewport height if smaller
            public boolean getScrollableTracksViewportHeight() {
                return getPreferredSize().height < getParent().getHeight();
            }
            // overridden to make the width of scroll match viewport width if smaller
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
        };
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		return table;
	}

}
