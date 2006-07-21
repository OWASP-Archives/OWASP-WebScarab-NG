/**
 * 
 */
package org.owasp.webscarab.util.swing.renderers;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author rdawes
 *
 */
public class TableColorRenderer implements TableCellRenderer {

	private TableCellRenderer delegate;
	private TableColorProvider provider;
	private boolean overrideSelection;
	
	public TableColorRenderer(TableCellRenderer delegate, TableColorProvider provider) {
		this(delegate, provider, false);
	}
	
	public TableColorRenderer(TableCellRenderer delegate, TableColorProvider provider, boolean overrideSelection) {
		this.delegate = delegate;
		this.provider = provider;
		this.overrideSelection = overrideSelection;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (isSelected && ! overrideSelection) {
			c.setForeground(table.getSelectionForeground());
			c.setBackground(table.getSelectionBackground());
		} else {
			c.setForeground(provider.getForegroundColor(table, value, isSelected, hasFocus, row, column));
			c.setBackground(provider.getBackGroundColor(table, value, isSelected, hasFocus, row, column));
		}
		return c;
	}

}
