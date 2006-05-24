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
		JXTable table = new JXTable();
		table.setColumnControlVisible(true);
		return table;
	}

	/* (non-Javadoc)
	 * @see org.springframework.richclient.factory.TableFactory#createTable(javax.swing.table.TableModel)
	 */
	public JTable createTable(TableModel model) {
		JXTable table = new JXTable(model);
		table.setColumnControlVisible(true);
		return table;
	}

}
