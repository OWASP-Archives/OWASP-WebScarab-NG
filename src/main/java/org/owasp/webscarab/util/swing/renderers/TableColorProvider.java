/**
 * 
 */
package org.owasp.webscarab.util.swing.renderers;

import java.awt.Color;

import javax.swing.JTable;

/**
 * @author rdawes
 *
 */
public interface TableColorProvider {

	Color getForegroundColor(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column);
	
	Color getBackGroundColor(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column);
	
}
