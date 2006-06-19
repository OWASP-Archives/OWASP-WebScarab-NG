/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.tree.AbstractTreeModel;
import org.springframework.util.ObjectUtils;

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
			Conversation.PROPERTY_REQUEST_CONTENT };

	private boolean readOnly;

	public ParsedRequestForm(FormModel model) {
		super(model, FORM_ID, Conversation.PROPERTY_REQUEST_HEADERS,
				Conversation.PROPERTY_REQUEST_CONTENT);
		readOnly = false;
		for (int i = 0; i < properties.length; i++)
			readOnly |= model.getFieldMetadata(properties[i]).isReadOnly();
	}

	@Override
	protected JComponent getParsedHeaderComponent() {
		JTree tree = new JTree(new ParsedRequestTreeModel(getFormModel()));
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

		private Object[] topLevelChildren;

		private String requestLine;

		private NamedValue[] queryParameters;

		private NamedValue[] headers;

		private int cookieIndex = -1;
		
		private NamedValue[] cookies;

		private RequestLineListener requestLineListener;

		private HeaderListener headerListener;

		private FormModel model;

		public ParsedRequestTreeModel(FormModel model) {
			super(model);
			this.model = model;
			requestLineListener = new RequestLineListener();
			headerListener = new HeaderListener();
			refreshRequestLine();
			refreshHeaders();
			model.getValueModel(Conversation.PROPERTY_REQUEST_METHOD)
					.addValueChangeListener(requestLineListener);
			model.getValueModel(Conversation.PROPERTY_REQUEST_URI)
					.addValueChangeListener(requestLineListener);
			model.getValueModel(Conversation.PROPERTY_REQUEST_VERSION)
					.addValueChangeListener(requestLineListener);
			model.getValueModel(Conversation.PROPERTY_REQUEST_HEADERS)
					.addValueChangeListener(headerListener);
		}

		private Object[] getChildren(Object parent) {
			if (parent == getRoot()) {
				return topLevelChildren;
			} else if (parent == requestLine) {
				return queryParameters;
			} else if (cookieIndex > -1 && parent == headers[cookieIndex]) {
				return cookies;
			} else
				return null;
		}

		public Object getChild(Object parent, int index) {
			return getChildren(parent)[index];
		}

		public int getChildCount(Object parent) {
			Object[] children = getChildren(parent);
			if (children == null || children.length == 0)
				return 0;
			return children.length;
		}

		public int getIndexOfChild(Object parent, Object child) {
			Object[] children = getChildren(parent);
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

		public void valueForPathChanged(TreePath path, Object newValue) {
			System.out.println("New value is " + newValue);
			if (path.getPathCount() == 2) {
				if (path.getLastPathComponent() instanceof String) {
					// the request line
					setRequestLine((String) newValue);
				} else if (path.getLastPathComponent() instanceof NamedValue) {
					// it's a header
					NamedValue header = (NamedValue) path
							.getLastPathComponent();
					NamedValue[] newHeaders = NamedValue.copy(headers);
					newHeaders = NamedValue.set(header.getName(),
							(String) newValue, newHeaders);
					setHeaders(newHeaders);
				}
			} else if (path.getPathCount() == 3) {
				if (path.getPathComponent(1) instanceof String) {
					// parent is the RequestLine, so a parameter value
					NamedValue parameter = (NamedValue) path
							.getLastPathComponent();
					NamedValue[] newParameters = NamedValue
							.copy(queryParameters);
					int index = searchNamedValues(queryParameters, parameter);
					if (index < 0) {
						System.out.println("NOT FOUND!!");
						return;
					}
					newParameters[index] = new NamedValue(parameter.getName(),
							(String) newValue);
					setQueryParameters(newParameters);
				} else if (path.getPathComponent(1) instanceof NamedValue) {
					// parent is a NamedValue, so a cookie value
					NamedValue cookie = (NamedValue) path
							.getLastPathComponent();
					NamedValue[] newCookies = NamedValue.copy(cookies);
					int index = searchNamedValues(cookies, cookie);
					if (index < 0) {
						System.out.println("NOT FOUND!!");
						return;
					}
					newCookies[index] = new NamedValue(cookie.getName(),
							(String) newValue);
					setCookieValues(newCookies);
				}
			} else {
				System.out.println("New value for " + path + " is " + newValue);
			}
		}

		private int searchNamedValues(NamedValue[] original, NamedValue modified) {
			for (int i = 0; i < original.length; i++) {
				if (original[i] == modified) {
					return i;
				}
			}
			return -1;
		}

		private void setRequestLine(String requestLine) {
			String[] parts = requestLine.split(" ");
			ValueModel vm = model
					.getValueModel(Conversation.PROPERTY_REQUEST_METHOD);
			if (parts.length > 0 && parts[0] != null && parts[0].length() != 0) {
				vm.setValueSilently(parts[0], requestLineListener);
			} else {
				vm.setValueSilently(null, requestLineListener);
			}

			vm = model.getValueModel(Conversation.PROPERTY_REQUEST_URI);
			if (parts.length > 1 && parts[1] != null && parts[1].length() != 0) {
				try {
					vm.setValueSilently(new URI(parts[1]), requestLineListener);
				} catch (URISyntaxException use) {
					vm.setValueSilently(null, requestLineListener);
				}
			} else {
				vm.setValueSilently(null, requestLineListener);
			}

			vm = model.getValueModel(Conversation.PROPERTY_REQUEST_VERSION);
			if (parts.length > 2 && parts[2] != null && parts[2].length() != 0) {
				vm.setValueSilently(parts[2], requestLineListener);
			} else {
				vm.setValueSilently(null, requestLineListener);
			}
			refreshRequestLine();
		}

		private void refreshRequestLine() {
			String oldRequestLine = requestLine;
			StringBuilder builder = new StringBuilder();
			ValueModel vm = model
					.getValueModel(Conversation.PROPERTY_REQUEST_METHOD);
			builder.append(vm.getValue()).append(" ");
			vm = model.getValueModel(Conversation.PROPERTY_REQUEST_URI);
			builder.append(vm.getValue()).append(" ");
			vm = model.getValueModel(Conversation.PROPERTY_REQUEST_VERSION);
			builder.append(vm.getValue());
			requestLine = builder.toString();
			updateTopLevelChildren();
			if (!ObjectUtils.nullSafeEquals(oldRequestLine, requestLine)) {
				fireTreeNodeChanged(new Object[] { getRoot() }, 0, requestLine);
			}
			refreshQueryParameters();
		}

		private void refreshQueryParameters() {
			NamedValue[] oldQueryParameters = queryParameters;
			ValueModel vm = model
					.getValueModel(Conversation.PROPERTY_REQUEST_URI);
			URI uri = (URI) vm.getValue();
			if (uri == null || uri.getQuery() == null) {
				queryParameters = null;
			} else {
				try {
					queryParameters = NamedValue
							.parse(uri.getQuery(), "&", "=");
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					queryParameters = null;
				}
			}
			// fire appropriate events
			if (oldQueryParameters != null && queryParameters == null) {
				int[] indices = new int[oldQueryParameters.length];
				for (int i = 0; i < oldQueryParameters.length; i++)
					indices[i] = i;
				fireTreeNodesRemoved(new Object[] { getRoot(), requestLine },
						indices, oldQueryParameters);
			} else if (oldQueryParameters == null && queryParameters != null) {
				int[] indices = createIndices(queryParameters.length);
				fireTreeNodesInserted(new Object[] { getRoot(), requestLine },
						indices, queryParameters);
			} else if (oldQueryParameters == null && queryParameters == null) {
				// do nothing
			} else if (oldQueryParameters.length == queryParameters.length) {
				TreeMap<Integer, NamedValue> changed = new TreeMap<Integer, NamedValue>();
				for (int i = 0; i < queryParameters.length; i++)
					if (oldQueryParameters[i].equals(queryParameters[i])) {
						queryParameters[i] = oldQueryParameters[i];
					} else {
						changed.put(new Integer(i), queryParameters[i]);
					}
				if (changed.size() > 0) {
					int[] indices = new int[changed.size()];
					NamedValue[] changedChildren = new NamedValue[changed
							.size()];
					Iterator<Integer> it = changed.keySet().iterator();
					for (int i = 0; i < changed.size(); i++) {
						Integer index = it.next();
						indices[i] = index.intValue();
						changedChildren[i] = changed.get(index);
					}
					fireTreeNodesChanged(
							new Object[] { getRoot(), requestLine }, indices,
							changedChildren);
				}
			} else {
				fireTreeStructureChanged(new Object[] { getRoot() }, 0,
						requestLine);
			}
		}

 		private void setQueryParameters(NamedValue[] parameters) {
			ValueModel vm = model
					.getValueModel(Conversation.PROPERTY_REQUEST_URI);
			URI uri = (URI) vm.getValue();
			String u = uri.toString();
			if (u.indexOf("?") > -1)
				u = u.substring(0, u.indexOf("?"));
			System.out.println("Trimmed url is '" + u + "'");
			if (parameters != null && parameters.length > 0) {
				StringBuilder builder = new StringBuilder(u);
				builder.append("?");
				for (int i = 0; i < parameters.length; i++) {
					System.out.println("Parameter is " + parameters[i]);
					builder.append(parameters[i].getName());
					if (parameters[i].getValue() != null)
						builder.append("=").append(parameters[i].getValue());
					builder.append("&");
				}
				// trim the last "&"
				u = builder.substring(0, builder.length() - 1);
			}
			System.out.println("Replacement URL is '" + u + "'");
			try {
				uri = new URI(u);
				vm.setValueSilently(uri, requestLineListener);
			} catch (URISyntaxException use) {
				use.printStackTrace();
				vm.setValueSilently(null, requestLineListener);
			}
			refreshRequestLine();
		}

 		private int[] createIndices(int length) {
 			int[] indices = new int[length];
 			for (int i=0; i< length; i++)
 				indices[i] = i;
 			return indices;
 		}
 		
		private void refreshHeaders() {
			NamedValue[] oldHeaders = headers;
			int oldCookieIndex = cookieIndex;
			NamedValue[] oldCookies = cookies;
			ValueModel vm = model
					.getValueModel(Conversation.PROPERTY_REQUEST_HEADERS);
			headers = (NamedValue[]) vm.getValue();
			cookieIndex = -1;
			if (headers != null)
				for (int i=0;i<headers.length; i++)
					if (headers[i].getName().equals("Cookie"))
						cookieIndex = i;
			if (oldCookieIndex > 0 && cookieIndex < 0) {
				// remove the old cookies before we change the headers
				cookies = null;
				int[] indices = createIndices(oldCookies.length);
				fireTreeNodesRemoved(new Object[] { getRoot(), oldHeaders[oldCookieIndex]}, indices, oldCookies);
			}
			updateTopLevelChildren();
			// fire appropriate events
			if (oldHeaders != null && headers == null) {
				int[] indices = new int[oldHeaders.length];
				for (int i = 0; i < oldHeaders.length; i++)
					indices[i] = i + 1;
				fireTreeNodesRemoved(new Object[] { getRoot() }, indices,
						oldHeaders);
			} else if (oldHeaders == null && headers != null) {
				int[] indices = new int[headers.length];
				for (int i = 0; i < headers.length; i++)
					indices[i] = i + 1;
				fireTreeNodesInserted(new Object[] { getRoot() }, indices,
						headers);
			} else if (oldHeaders == null && headers == null) {
				// do nothing
			} else if (oldHeaders.length == headers.length) {
				TreeMap<Integer, NamedValue> changed = new TreeMap<Integer, NamedValue>();
				for (int i = 0; i < headers.length; i++)
					if (oldHeaders[i].equals(headers[i])) {
						headers[i] = oldHeaders[i];
					} else {
						changed.put(new Integer(i + 1), headers[i]);
					}
				if (changed.size() > 0) {
					int[] indices = new int[changed.size()];
					NamedValue[] changedChildren = new NamedValue[changed
							.size()];
					Iterator<Integer> it = changed.keySet().iterator();
					for (int i = 0; i < changed.size(); i++) {
						Integer index = it.next();
						indices[i] = index.intValue();
						changedChildren[i] = changed.get(index);
					}
					fireTreeNodesChanged(new Object[] { getRoot() }, indices,
							changedChildren);
				}
			} else {
				fireRootTreeStructureChanged(getRoot());
			}
			if (cookieIndex > -1) {
				// we might have to refresh the cookies
				if (oldCookieIndex > -1 && oldHeaders[oldCookieIndex].getValue().equals(headers[cookieIndex].getValue())) {
					// do nothing, it just moved around a little?
				} else {
					try {
						cookies = NamedValue.parse(headers[cookieIndex].getValue(), "; ", "=");
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						cookies = null;
						cookieIndex = -1;
					}
					if (oldCookies == null && cookies != null) {
						int[] indices = createIndices(cookies.length);
						fireTreeNodesInserted(new Object[] { getRoot(), headers[cookieIndex] },
								indices, cookies);
					} else if (oldCookies == null && cookies == null) {
						// do nothing
					} else if (oldCookies.length == cookies.length) {
						TreeMap<Integer, NamedValue> changed = new TreeMap<Integer, NamedValue>();
						for (int i = 0; i < cookies.length; i++)
							if (oldCookies[i].equals(cookies[i])) {
								cookies[i] = oldCookies[i];
							} else {
								changed.put(new Integer(i), cookies[i]);
							}
						if (changed.size() > 0) {
							int[] indices = new int[changed.size()];
							NamedValue[] changedChildren = new NamedValue[changed
									.size()];
							Iterator<Integer> it = changed.keySet().iterator();
							for (int i = 0; i < changed.size(); i++) {
								Integer index = it.next();
								indices[i] = index.intValue();
								changedChildren[i] = changed.get(index);
							}
							fireTreeNodesChanged(new Object[] { getRoot(), headers[cookieIndex] },
									indices, changedChildren);
						}
					} else {
						fireTreeStructureChanged(new Object[] { getRoot() }, cookieIndex,
									headers[cookieIndex]);
					}
				}
			}
		}

		private void setHeaders(NamedValue[] headers) {
			ValueModel vm = model
					.getValueModel(Conversation.PROPERTY_REQUEST_HEADERS);
			vm.setValueSilently(headers, headerListener);
			refreshHeaders();
		}

		private void setCookieValues(NamedValue[] newCookies) {
			NamedValue[] headers = this.headers;
			if (newCookies == null || newCookies.length == 0) {
				headers = NamedValue.delete("Cookie", headers);
			} else {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < newCookies.length; i++) {
					builder.append(newCookies[i].getName());
					if (newCookies[i].getValue() != null) {
						builder.append("=").append(newCookies[i].getValue());
					}
					builder.append("; ");
				}
				String value = builder.substring(0, builder.length() - 2);
				headers = NamedValue.set("Cookie", value, headers);
			}
			setHeaders(headers);
		}

		private void updateTopLevelChildren() {
			if (headers == null || headers.length == 0) {
				topLevelChildren = new Object[] { requestLine };
			} else {
				topLevelChildren = new Object[headers.length + 1];
				System.arraycopy(headers, 0, topLevelChildren, 1,
						headers.length);
				topLevelChildren[0] = requestLine;
			}
		}

		private class RequestLineListener implements PropertyChangeListener {
			public void propertyChange(PropertyChangeEvent evt) {
				refreshRequestLine();
			}
		}

		private class HeaderListener implements PropertyChangeListener {
			public void propertyChange(PropertyChangeEvent evt) {
				 refreshHeaders();
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
}
