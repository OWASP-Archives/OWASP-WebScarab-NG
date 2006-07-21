/**
 * 
 */
package org.owasp.webscarab.util.swing.renderers;

import java.awt.Component;
import java.awt.FontMetrics;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author rdawes
 * 
 */
public class DateRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -3574289157340405523L;

	private static SimpleDateFormat[] formats = new SimpleDateFormat[] {
		new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"),
		new SimpleDateFormat("MMM dd HH:mm:ss"),
		new SimpleDateFormat("HH:mm:ss"), 
		new SimpleDateFormat(":mm:ss")
	};

	public DateRenderer() {
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof Date) {
			Date date = (Date) value;
			FontMetrics fm = getFontMetrics(getFont());
			int i = 0;
			String text;
			int textWidth;
			int targetWidth = table.getColumnModel().getColumn(column).getWidth() - 4;
			do {
				text = formats[i++].format(date); 
				textWidth = fm.stringWidth( text );
			} while (textWidth > targetWidth && i < formats.length);
			setText(text);
		}
		return this;
	}
}
