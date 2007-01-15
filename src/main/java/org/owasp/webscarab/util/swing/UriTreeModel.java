/**
 *
 */
package org.owasp.webscarab.util.swing;

import javax.swing.tree.TreePath;

import org.owasp.webscarab.util.UrlUtils;
import org.springframework.richclient.tree.AbstractTreeModel;

import java.net.URI;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author rdawes
 *
 */
public class UriTreeModel extends AbstractTreeModel {

	private static final URI[] NO_CHILDREN = new URI[0];

	private static final Comparator<URI> comp = new UriComparator();

	private Map<URI, URI[]> nodes = new TreeMap<URI, URI[]>(comp);

	private Set<URI> implied = new HashSet<URI>();

	public UriTreeModel() {
		super("Root");
	}

	public boolean add(URI uri) {
		return add(uri, false);
	}

	private boolean add(URI uri, boolean implicit) {
		URI parent = UrlUtils.getParent(uri);
		if (implied.contains(uri) && !implicit) {
			implied.remove(uri);
			fireTreeNodeChanged(path(parent), getIndexOfChild(parent, uri), uri);
		}
		if (nodes.containsKey(uri))
			return false;
		if (parent != null)
			add(parent, true);
		int position = insertChild(parent, uri);
		if (implicit)
			implied.add(uri);
		if (position < 0) {
			Object[] path = path(parent);
			fireTreeNodeInserted(path, -position - 1, uri);
		}
		return true;
	}

	private Object[] path(URI node) {
		List<Object> path = new LinkedList<Object>();
		if (node != null) {
			path.add(0, node);
			URI parent = node;
			while ((parent = UrlUtils.getParent(parent)) != null) {
				path.add(0, parent);
			}
		}
		path.add(0, getRoot());
		return path.toArray();
	}

	private int insertChild(URI parent, URI child) {
		URI[] children = getChildren(parent);
		int position = getIndexOfChild(parent, child);
		if (position < 0) {
			URI[] newChildren = new URI[children.length + 1];
			System.arraycopy(children, 0, newChildren, 0, -position - 1);
			System.arraycopy(children, -position - 1, newChildren, -position,
					children.length + position + 1);
			newChildren[-position - 1] = child;
			nodes.put(parent, newChildren);
			nodes.put(child, NO_CHILDREN);
		}
		return position;
	}

	public boolean remove(URI uri) {
		URI parent = UrlUtils.getParent(uri);
		URI[] children = getChildren(uri);
		if (children.length > 0) {
			implied.add(uri);
			fireTreeNodeChanged(path(parent), getIndexOfChild(parent, uri), uri);
			return false;
		}
		int position = removeChild(parent, uri);
		if (position < 0) {
			return false;
		}
		fireTreeNodeRemoved(path(parent), position, uri);
		if (isImplied(parent) && getChildCount(parent) == 0)
			remove(parent);
		return true;

	}

    public void clear() {
        nodes.clear();
        implied.clear();
        fireRootNodeChanged(getRoot());
    }

	private int removeChild(URI parent, URI child) {
		URI[] children = getChildren(parent);
		int position = getIndexOfChild(parent, child);
		if (position > -1) {
			URI[] newChildren;
			if (children.length == 1) {
				newChildren = NO_CHILDREN;
			} else {
				newChildren = new URI[children.length - 1];
				System.arraycopy(children, 0, newChildren, 0, position);
				System.arraycopy(children, position + 1, newChildren, position,
						children.length - position - 1);
			}
			nodes.put(parent, newChildren);
			nodes.remove(child);
		}
		return position;
	}

	public boolean isImplied(URI uri) {
		return implied.contains(uri);
	}

	private URI[] getChildren(Object parent) {
		URI[] children;
		if (parent == getRoot()) {
			children = nodes.get(null);
		} else {
			children = nodes.get(parent);
		}
		if (children == null)
			children = NO_CHILDREN;
		return children;
	}

	public Object getChild(Object parent, int index) {
		return getChildren(parent)[index];
	}

	public int getChildCount(Object parent) {
		return getChildren(parent).length;
	}

	/*
	 * This method implements TreeModel.getIndexOfChild(Object, Object), with an additional twist.
	 * It can also be used to tell callers the position where the child node would be inserted
	 * amongst its siblings. Nodes are sorted according to the <code>UriComparator<code>
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child) {
		URI[] children = getChildren(parent);
		for (int i = 0; i < children.length; i++) {
			int result = comp.compare((URI) child, children[i]);
			if (result == 0) {
				return i;
			} else if (result < 0) {
				return -i - 1;
			}
		}
		return -children.length - 1;
	}

	public boolean isLeaf(Object node) {
		if (node == getRoot()) return false;
		if (node.toString().endsWith("/"))
			return false;
		URI[] children = getChildren(node);
		return (children == null || children.length == 0);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		// we don't support editing
	}

	private static class UriComparator implements Comparator<URI> {

		public UriComparator() {
		}

		public int compare(URI u1, URI u2) {
			if (u1 == null && u2 == null)
				return 0;
			if (u1 == null)
				return -1;
			if (u2 == null)
				return 1;
			int result = u1.getHost().toLowerCase().compareTo(
					u2.getHost().toLowerCase());
			if (result != 0)
				return result;
			result = u1.getScheme().compareTo(u2.getScheme());
			if (result != 0)
				return result;
			result = u2.getPort() - u1.getPort();
			if (result != 0)
				return result;
			result = u1.getPath().compareTo(u2.getPath());
			if (result != 0)
				return result;
			String q1 = u1.getQuery() == null ? "" : u1.getQuery();
			String q2 = u2.getQuery() == null ? "" : u2.getQuery();
			result = q1.compareTo(q2);
			return result;
		}

	}

}
