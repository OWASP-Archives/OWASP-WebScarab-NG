/**
 * 
 */
package org.owasp.webscarab.util.swing.renderers;

import java.awt.Component;
import java.net.URI;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.owasp.webscarab.util.UrlUtils;

/**
 * @author rdawes
 *
 */
public class UriRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 6598912493169873948L;

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		if (value instanceof URI) {
			URI uri = (URI) value;
			URI parent = UrlUtils.getParent(uri);
			if (parent != null) {
				setText(uri.toString().substring(parent.toString().length()));
			}
		}
		return c;
	}

}
