/**
 *
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.util.UrlUtils;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.tree.AbstractTreeModel;

/**
 * @author rdawes
 *
 */
public class ParsedRequestForm extends AbstractParsedContentForm {

	private static final String FORM_ID = "parsedRequestForm";

	private static final String[] properties = {
			Conversation.PROPERTY_REQUEST_METHOD,
			Conversation.PROPERTY_REQUEST_URI,
			Conversation.PROPERTY_REQUEST_VERSION,
			Conversation.PROPERTY_REQUEST_HEADERS,
			Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT };

	private boolean readOnly;

	public ParsedRequestForm(FormModel model) {
		super(model, FORM_ID, Conversation.PROPERTY_REQUEST_HEADERS,
				Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT);
		readOnly = false;
		for (int i = 0; i < properties.length; i++)
			readOnly |= model.getFieldMetadata(properties[i]).isReadOnly();
	}

	@Override
	protected JComponent getParsedHeaderComponent() {
		ParsedRequestTreeModel treeModel = new ParsedRequestTreeModel(
				getFormModel());
		final JTree tree = new JTree(treeModel);
		new ExpansionListener(tree, treeModel);
		DefaultTreeCellRenderer renderer = new NamedValueTreeCellRenderer();
		tree.setCellRenderer(renderer);
		if (!readOnly) {
			DefaultTreeCellEditor editor = new NamedValueTreeCellEditor(tree,
					renderer);
			tree.setCellEditor(editor);
			tree.setEditable(true);
		}
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		return getComponentFactory().createScrollPane(tree);
	}

	private static class ParsedRequestTreeModel extends AbstractTreeModel {

		private Map<Object, Object[]> nodes = new HashMap<Object, Object[]>();

		private ChangeListener changeListener;

		private ValueModel methodVM, uriVM, versionVM, headerVM;

		public ParsedRequestTreeModel(FormModel model) {
			super(model);
			changeListener = new ChangeListener();
			methodVM = model
					.getValueModel(Conversation.PROPERTY_REQUEST_METHOD);
			uriVM = model.getValueModel(Conversation.PROPERTY_REQUEST_URI);
			versionVM = model
					.getValueModel(Conversation.PROPERTY_REQUEST_VERSION);
			headerVM = model
					.getValueModel(Conversation.PROPERTY_REQUEST_HEADERS);
			updateNodes();
			methodVM.addValueChangeListener(changeListener);
			uriVM.addValueChangeListener(changeListener);
			versionVM.addValueChangeListener(changeListener);
			headerVM.addValueChangeListener(changeListener);
		}

		private void updateNodes() {
			Map<Object, Object[]> oldNodes = nodes;
			nodes = new HashMap<Object, Object[]>();
			StringBuilder requestLine = new StringBuilder();
			if (methodVM.getValue() != null)
				requestLine.append(methodVM.getValue()).append(" ");
			if (uriVM.getValue() != null)
				requestLine.append(uriVM.getValue()).append(" ");
			if (versionVM.getValue() != null)
				requestLine.append(versionVM.getValue());
			NamedValue[] headers = (NamedValue[]) headerVM.getValue();
			Object[] top;
			if (headers != null) {
				top = new Object[headers.length + 1];
				System.arraycopy(headers, 0, top, 1, headers.length);
				for (int i = 0; i < headers.length; i++) {
					if (headers[i].getName().equalsIgnoreCase("cookie")) {
						try {
							NamedValue[] cookies = NamedValue.parse(headers[i]
									.getValue(), "; *", "=");
							nodes.put(headers[i], cookies);
						} catch (Exception e) {
							System.out.println("Error parsing cookie value: "
									+ e);
						}
					}
				}
			} else {
				top = new Object[1];
			}
			top[0] = requestLine.toString();
			nodes.put(getRoot(), top);
			if (uriVM.getValue() != null) {
				URI uri = (URI) uriVM.getValue();
				String query = uri.getQuery();
				if (query != null) {
					try {
						NamedValue[] params = NamedValue.parse(query, "&", "=");
						nodes.put(top[0], params);
					} catch (Exception e) {
						System.out
								.println("Error parsing URL parameters: " + e);
					}
				}
			}
			calculateAndFireChangeEvents(oldNodes, nodes);
		}

		private void calculateAndFireChangeEvents(
				Map<Object, Object[]> oldNodes, Map<Object, Object[]> nodes) {
			Object[] rootPath = new Object[] { getRoot() };
			// compare top level children
			Object[] oldChildren = oldNodes.get(getRoot());
			Object[] newChildren = nodes.get(getRoot());
			if (oldChildren == null || newChildren == null
					|| oldChildren.length != newChildren.length) {
				// chicken out if the lengths are different
				fireRootTreeStructureChanged(getRoot());
				return;
			}
			if (!oldChildren[0].equals(newChildren[0])) { // the request line
				// we can do better, but for the moment . . .
				fireTreeStructureChanged(rootPath, 0, newChildren[0]);
			}
			for (int i = 1; i < oldChildren.length; i++) { // the headers
				NamedValue oldHeader = (NamedValue) oldChildren[i];
				NamedValue newHeader = (NamedValue) newChildren[i];
				if (!oldHeader.equals(newHeader)) {
					fireTreeNodeChanged(rootPath, i, newHeader);
					String oldName = oldHeader.getName();
					String newName = newHeader.getName();
					if (oldName.equalsIgnoreCase("Cookie")
							|| newName.equalsIgnoreCase("Cookie")) {
						if (!oldName.equalsIgnoreCase(newName)) {
							fireTreeStructureChanged(rootPath, i, newHeader);
						} else {
							NamedValue[] oldCookies = (NamedValue[]) oldNodes
									.get(oldHeader);
							NamedValue[] newCookies = (NamedValue[]) nodes
									.get(newHeader);
							if (oldCookies == null || newCookies == null
									|| oldCookies.length != newCookies.length) {
								fireTreeStructureChanged(rootPath, i, newHeader);
							} else {
								Object[] cookiePath = new Object[] { getRoot(),
										newHeader };
								for (int j = 0; j < oldCookies.length; j++) {
									if (!oldCookies[j].equals(newCookies[j]))
										fireTreeNodeChanged(cookiePath, j,
												newCookies[j]);
								}
							}
						}
					}
				}
			}
		}

		public Object getChild(Object parent, int index) {
			return nodes.get(parent)[index];
		}

		public int getChildCount(Object parent) {
			Object[] children = nodes.get(parent);
			if (children == null || children.length == 0)
				return 0;
			return children.length;
		}

		public int getIndexOfChild(Object parent, Object child) {
			Object[] children = nodes.get(parent);
			if (children == null) {
				System.out.println("Got no children under " + parent);
				return -1;
			}
			for (int i = 0; i < children.length; i++)
				if (child == children[i])
					return i;
			return -1;
		}

		public boolean isLeaf(Object node) {
			return getChildCount(node) == 0;
		}

		public TreePath getPathToRequestLine() {
			Object[] top = nodes.get(getRoot());
			if (top.length < 1)
				return null;
			return new TreePath(new Object[] { getRoot(), top[0] });
		}

		public TreePath getPathToCookie() {
			Object[] top = nodes.get(getRoot());
			for (int i = 1; i < top.length; i++) {
				if (top[i] instanceof NamedValue) {
					NamedValue nv = (NamedValue) top[i];
					if (nv.getName().equalsIgnoreCase("cookie"))
						return new TreePath(new Object[] { getRoot(), nv });
				}
			}
			return null;
		}

		public void insertNode(TreePath path, int index, Object node) {
			Object parent = path.getLastPathComponent();
			NamedValue nv = (NamedValue) node;
			if (parent == getRoot()) { // adding a header
				int realIndex = index - 1;
				NamedValue[] oldHeaders = (NamedValue[]) headerVM.getValue();
				NamedValue[] newHeaders;
				if (oldHeaders == null || oldHeaders.length == 0) {
					newHeaders = new NamedValue[] { nv };
				} else {
					newHeaders = new NamedValue[oldHeaders.length + 1];
					System.arraycopy(oldHeaders, 0, newHeaders, 0, realIndex);
					System.arraycopy(oldHeaders, realIndex, newHeaders,
							realIndex + 1, oldHeaders.length - realIndex);
					newHeaders[realIndex] = nv;
				}
				headerVM.setValue(newHeaders);
			} else if (parent instanceof String) { // adding a parameter
				NamedValue[] oldParams = (NamedValue[]) nodes.get(parent);
				NamedValue[] newParams;
				if (oldParams == null || oldParams.length == 0) {
					newParams = new NamedValue[] { nv };
				} else {
					newParams = new NamedValue[oldParams.length + 1];
					System.arraycopy(oldParams, 0, newParams, 0, index);
					System.arraycopy(oldParams, index, newParams, index + 1,
							oldParams.length - index);
					newParams[index] = nv;
				}
				String query = NamedValue.join(newParams, "&", "=");
				URI uri = (URI) uriVM.getValue();
				String base = UrlUtils.getSchemeHostPort(uri) + uri.getPath();
				try {
					URI newUri = new URI(base + "?" + query);
					uriVM.setValue(newUri);
				} catch (Exception e) {
					System.out.println("Exception parsing url : " + e);
				}
			} else if (parent instanceof NamedValue) { // adding a cookie
				NamedValue[] oldCookies = (NamedValue[]) nodes.get(parent);
				NamedValue[] newCookies;
				if (oldCookies == null || oldCookies.length == 0) {
					newCookies = new NamedValue[] { nv };
				} else {
					newCookies = new NamedValue[oldCookies.length + 1];
					System.arraycopy(oldCookies, 0, newCookies, 0, index);
					System.arraycopy(oldCookies, index, newCookies, index + 1,
							oldCookies.length - index);
					newCookies[index] = nv;
				}
				String cookie = NamedValue.join(newCookies, "; ", "=");
				NamedValue newCookie = new NamedValue("Cookie", cookie);
				NamedValue oldCookie = (NamedValue) parent;
				NamedValue[] headers = (NamedValue[]) headerVM.getValue();
				NamedValue[] newHeaders = copyAndReplace(headers, oldCookie,
						newCookie);
				headerVM.setValue(newHeaders);
			}
		}

		public void deleteNode(TreePath path, int index, Object node) {
			Object parent = path.getLastPathComponent();
			NamedValue nv = (NamedValue) node;
			if (parent == getRoot()) { // deleting a header
				int realIndex = index - 1;
				NamedValue[] oldHeaders = (NamedValue[]) headerVM.getValue();
				NamedValue[] newHeaders;
				if (oldHeaders == null || oldHeaders.length <= 1) {
					newHeaders = null;
				} else {
					newHeaders = new NamedValue[oldHeaders.length - 1];
					System.arraycopy(oldHeaders, 0, newHeaders, 0,
							realIndex - 1);
					System.arraycopy(oldHeaders, realIndex + 1, newHeaders,
							realIndex, oldHeaders.length - realIndex - 1);
				}
				headerVM.setValue(newHeaders);
			} else if (parent instanceof String) { // adding a parameter
				NamedValue[] oldParams = (NamedValue[]) nodes.get(parent);
				NamedValue[] newParams;
				if (oldParams == null || oldParams.length <= 1) {
					newParams = null;
				} else {
					newParams = new NamedValue[oldParams.length - 1];
					System.arraycopy(oldParams, 0, newParams, 0, index - 1);
					System.arraycopy(oldParams, index + 1, newParams, index,
							oldParams.length - index - 1);
				}
				URI uri = (URI) uriVM.getValue();
				String base = UrlUtils.getSchemeHostPort(uri) + uri.getPath();
				String query = null;
				if (newParams != null) {
					query = NamedValue.join(newParams, "&", "=");
					try {
						URI newUri = new URI(base
								+ (query == null ? "" : "?" + query));
						uriVM.setValue(newUri);
					} catch (Exception e) {
						System.out.println("Exception parsing url : " + e);
					}
				} else if (parent instanceof NamedValue) { // adding a cookie
					NamedValue[] oldCookies = (NamedValue[]) nodes.get(parent);
					NamedValue[] newCookies;
					if (oldCookies == null || oldCookies.length <= 1) {
						newCookies = null;
					} else {
						newCookies = new NamedValue[oldCookies.length - 1];
						System.arraycopy(oldCookies, 0, newCookies, 0,
								index - 1);
						System.arraycopy(oldCookies, index + 1, newCookies,
								index, oldCookies.length - index - 1);
						newCookies[index] = nv;
					}
					String cookie = NamedValue.join(newCookies, "; ", "=");
					NamedValue newCookie = new NamedValue("Cookie", cookie);
					NamedValue oldCookie = (NamedValue) parent;
					NamedValue[] headers = (NamedValue[]) headerVM.getValue();
					NamedValue[] newHeaders = copyAndReplace(headers,
							oldCookie, newCookie);
					headerVM.setValue(newHeaders);
				}
			}
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			Object last = path.getLastPathComponent();
			System.out.println("New value for " + last + " is " + newValue);
			if (last instanceof String) { // request line
				String requestLine = (String) newValue;
				String[] parts = requestLine.split(" ");
				if (parts.length > 0 && parts[0] != null
						&& parts[0].length() != 0) {
					methodVM.setValue(parts[0]);
				} else {
					methodVM.setValue(null);
				}
				if (parts.length > 1 && parts[1] != null
						&& parts[1].length() != 0) {
					try {
						uriVM.setValue(new URI(parts[1]));
					} catch (URISyntaxException use) {
						uriVM.setValue(null);
					}
				} else {
					uriVM.setValue(null);
				}
				if (parts.length > 2 && parts[2] != null
						&& parts[2].length() != 0) {
					versionVM.setValue(parts[2]);
				} else {
					versionVM.setValue(null);
				}
				if (parts.length > 3) {
					System.out.println("Too many parts on the request line: "
							+ parts.length);
				}
			} else if (last instanceof NamedValue) {
				// parameter, header, cookie?
				NamedValue nv = (NamedValue) last;
				Object parent = path.getParentPath().getLastPathComponent();
				NamedValue newNv = new NamedValue(nv.getName(),
						(String) newValue);
				if (parent == getRoot()) { // header
					NamedValue[] headers = (NamedValue[]) headerVM.getValue();
					NamedValue[] newHeaders = copyAndReplace(headers, nv, newNv);
					headerVM.setValue(newHeaders);
				} else if (parent instanceof String) { // parameter
					NamedValue[] params = (NamedValue[]) nodes.get(parent);
					NamedValue[] newParams = copyAndReplace(params, nv, newNv);
					String query = NamedValue.join(newParams, "&", "=");
					URI uri = (URI) uriVM.getValue();
					String base = UrlUtils.getSchemeHostPort(uri)
							+ uri.getPath();
					try {
						URI newUri = new URI(base + "?" + query);
						uriVM.setValue(newUri);
					} catch (Exception e) {
						System.out.println("Exception parsing url : " + e);
					}
				} else if (parent instanceof NamedValue) { // cookie
					NamedValue[] cookies = (NamedValue[]) nodes.get(parent);
					NamedValue[] newCookies = copyAndReplace(cookies, nv, newNv);
					String cookie = NamedValue.join(newCookies, "; ", "=");
					NamedValue newCookie = new NamedValue("Cookie", cookie);
					NamedValue oldCookie = (NamedValue) parent;
					System.out.println("Old cookie was : '" + oldCookie + "'");
					System.out.println("New cookie is  : '" + newCookie + "'");
					NamedValue[] headers = (NamedValue[]) headerVM.getValue();
					NamedValue[] newHeaders = copyAndReplace(headers,
							oldCookie, newCookie);
					headerVM.setValue(newHeaders);
				}
			}
		}

		private NamedValue[] copyAndReplace(NamedValue[] nv, NamedValue was,
				NamedValue is) {
			NamedValue[] replacement = NamedValue.copy(nv);
			for (int i = 0; i < replacement.length; i++)
				if (replacement[i] == was)
					replacement[i] = is;
			return replacement;
		}

		private class ChangeListener implements PropertyChangeListener {
			public void propertyChange(PropertyChangeEvent evt) {
				updateNodes();
			}
		}

	}

	private class NamedValueTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 233428792355220860L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			if (value instanceof NamedValue) {
				NamedValue nv = (NamedValue) value;
				NamedValue[] headers = (NamedValue[]) getFormModel()
						.getValueModel(Conversation.PROPERTY_REQUEST_HEADERS)
						.getValue();
				boolean header = false;
				if (headers != null)
					for (int i = 0; i < headers.length; i++)
						if (nv == headers[i]) {
							header = true;
							break;
						}
				if (header) {
					value = nv.getName() + ": " + nv.getValue();
				} else {
					StringBuilder b = new StringBuilder(nv.getName());
					if (nv.getValue() != null)
						b.append("=").append(nv.getValue());
					value = b.toString();
				}
			}
			return super.getTreeCellRendererComponent(tree, value, sel,
					expanded, leaf, row, hasFocus);
		}

	}

	private class NamedValueTreeCellEditor extends DefaultTreeCellEditor {

		public NamedValueTreeCellEditor(JTree tree,
				DefaultTreeCellRenderer renderer) {
			super(tree, renderer);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row) {
			if (value instanceof NamedValue)
				value = ((NamedValue) value).getValue();
			return super.getTreeCellEditorComponent(tree, value, isSelected,
					expanded, leaf, row);
		}

		protected boolean canEditImmediately(EventObject event) {
			if ((event instanceof MouseEvent)
					&& SwingUtilities.isLeftMouseButton((MouseEvent) event)) {
				MouseEvent me = (MouseEvent) event;

				return ((me.getClickCount() == 2) && inHitRegion(me.getX(), me
						.getY()));
			}
			return (event == null);
		}
	}

	private class ExpansionListener implements TreeExpansionListener,
			TreeModelListener {
		private boolean urlExpanded = false;

		private boolean cookiesExpanded = false;

		private JTree tree;

		private ParsedRequestTreeModel treeModel;

		public ExpansionListener(JTree tree, ParsedRequestTreeModel treeModel) {
			tree.addTreeExpansionListener(this);
			tree.getModel().addTreeModelListener(this);
			this.tree = tree;
			this.treeModel = treeModel;
		}

		public void treeNodesChanged(TreeModelEvent e) {
			update(e);
		}

		public void treeNodesInserted(TreeModelEvent e) {
			update(e);
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			update(e);
		}

		public void treeStructureChanged(TreeModelEvent e) {
			update(e);
		}

		public void treeCollapsed(TreeExpansionEvent event) {
			update(event, false);
		}

		public void treeExpanded(TreeExpansionEvent event) {
			update(event, true);
		}

		private void update(TreeExpansionEvent event, boolean expanded) {
			TreePath urlPath = treeModel.getPathToRequestLine();
			if (urlPath != null && event.getPath().equals(urlPath)) {
				urlExpanded = expanded;
			}
			TreePath cookiePath = treeModel.getPathToCookie();
			if (cookiePath != null && event.getPath().equals(cookiePath)) {
				cookiesExpanded = expanded;
			}
		}

		private void update(TreeModelEvent event) {
			final TreePath urlPath = treeModel.getPathToRequestLine();
			if (urlPath != null && urlExpanded)
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tree.expandPath(urlPath);
					}
				});
			final TreePath cookiePath = treeModel.getPathToCookie();
			if (cookiePath != null && cookiesExpanded)
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tree.expandPath(cookiePath);
					}
				});
		}
	}

}
