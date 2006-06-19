/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;

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

/**
 * @author rdawes
 * 
 */
public class ParsedResponseForm extends AbstractParsedContentForm {

	private static final String FORM_ID = "parsedResponseForm";
	
	private static final String[] properties = {
		Conversation.PROPERTY_RESPONSE_VERSION,
		Conversation.PROPERTY_RESPONSE_STATUS,
		Conversation.PROPERTY_RESPONSE_MESSAGE,
		Conversation.PROPERTY_RESPONSE_HEADERS,
		Conversation.PROPERTY_RESPONSE_CONTENT };

	private boolean readOnly;
	
	public ParsedResponseForm(FormModel model) {
		super(model, FORM_ID, Conversation.PROPERTY_RESPONSE_HEADERS,
				Conversation.PROPERTY_RESPONSE_CONTENT);
		readOnly = false;
		for (int i = 0; i < properties.length; i++)
			readOnly |= model.getFieldMetadata(properties[i]).isReadOnly();
	}

	@Override
	protected JComponent getParsedHeaderComponent() {
		JTree tree = new JTree(new ParsedResponseTreeModel(getFormModel()));
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

	private static class ParsedResponseTreeModel extends AbstractTreeModel {

		private Object[] topLevelChildren;
		
		private String responseLine;

		private VersionListener versionListener;

		private StatusListener statusListener;

		private MessageListener messageListener;

		private HeaderListener headerListener;

		private FormModel model;
		
		public ParsedResponseTreeModel(FormModel model) {
			super(model);
			this.model = model;
			versionListener = new VersionListener();
			statusListener = new StatusListener();
			messageListener = new MessageListener();
			headerListener = new HeaderListener();
			model.getValueModel(Conversation.PROPERTY_RESPONSE_VERSION)
					.addValueChangeListener(versionListener);
			model.getValueModel(Conversation.PROPERTY_RESPONSE_STATUS)
					.addValueChangeListener(statusListener);
			model.getValueModel(Conversation.PROPERTY_RESPONSE_MESSAGE)
					.addValueChangeListener(messageListener);
			model.getValueModel(Conversation.PROPERTY_RESPONSE_HEADERS)
					.addValueChangeListener(headerListener);
			responseLine = getResponseLine();
			updateTopLevelChildren();
		}

		private Object[] getChildren(Object parent) {
			if (parent == getRoot()) {
				return topLevelChildren;
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
			for (int i=0; i<children.length; i++)
				if (child == children[i]) return i;
			return -1;
		}

		public boolean isLeaf(Object node) {
			return getChildCount(node) == 0;
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			// TODO Auto-generated method stub
			System.out.println("New value for " + path + " is " + newValue);
		}

		private String getResponseLine() {
			StringBuilder builder = new StringBuilder();
			ValueModel vm = model.getValueModel(Conversation.PROPERTY_RESPONSE_VERSION);
			builder.append(vm.getValue()).append(" ");
			vm = model.getValueModel(Conversation.PROPERTY_RESPONSE_STATUS);
			builder.append(vm.getValue()).append(" ");
			vm = model.getValueModel(Conversation.PROPERTY_RESPONSE_MESSAGE);
			builder.append(vm.getValue());
			return builder.toString();
		}

		private void updateResponseLine(String responseLine) {
			String[] parts = responseLine.split(" ");
			ValueModel vm = model.getValueModel(Conversation.PROPERTY_RESPONSE_VERSION);
			if (parts.length > 0 && parts[0] != null && parts[0].length() != 0) {
				vm.setValueSilently(parts[0], versionListener);
			} else {
				vm.setValueSilently(null, versionListener);
			}

			vm = model.getValueModel(Conversation.PROPERTY_RESPONSE_STATUS);
			if (parts.length > 1 && parts[1] != null && parts[1].length() != 0) {
				vm.setValueSilently(parts[1], statusListener);
			} else {
				vm.setValueSilently(null, statusListener);
			}

			vm = model.getValueModel(Conversation.PROPERTY_RESPONSE_MESSAGE);
			if (parts.length > 2 && parts[2] != null && parts[2].length() != 0) {
				vm.setValueSilently(parts[2], messageListener);
			} else {
				vm.setValueSilently(null, messageListener);
			}
		}

		private NamedValue[] getHeaders() {
			ValueModel vm = model.getValueModel(Conversation.PROPERTY_RESPONSE_HEADERS);
			return (NamedValue[]) vm.getValue();
		}
		
		private void setHeaders(NamedValue[] headers) {
			ValueModel vm = model.getValueModel(Conversation.PROPERTY_RESPONSE_HEADERS);
			vm.setValueSilently(headers, headerListener);
		}
		
		private void updateTopLevelChildren() {
			NamedValue[] headers = getHeaders();
			if (headers == null || headers.length == 0) {
				topLevelChildren = new Object[] { responseLine };
			} else {
				topLevelChildren = new Object[headers.length + 1];
				System.arraycopy(headers, 0, topLevelChildren, 1, headers.length);
				topLevelChildren[0] = responseLine;
			}
		}
		
		private class VersionListener implements PropertyChangeListener {
			public void propertyChange(PropertyChangeEvent evt) {
				responseLine = getResponseLine();
				updateTopLevelChildren();
				fireRootTreeStructureChanged(getRoot());
			}
		}

		private class StatusListener implements PropertyChangeListener {
			public void propertyChange(PropertyChangeEvent evt) {
				responseLine = getResponseLine();
				updateTopLevelChildren();
				fireRootTreeStructureChanged(getRoot());
			}
		}

		private class MessageListener implements PropertyChangeListener {
			public void propertyChange(PropertyChangeEvent evt) {
				responseLine = getResponseLine();
				updateTopLevelChildren();
				fireRootTreeStructureChanged(getRoot());
			}
		}

		private class HeaderListener implements PropertyChangeListener {
			public void propertyChange(PropertyChangeEvent evt) {
				updateTopLevelChildren();
				fireRootTreeStructureChanged(getRoot());
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
						.getValueModel(Conversation.PROPERTY_RESPONSE_HEADERS)
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
