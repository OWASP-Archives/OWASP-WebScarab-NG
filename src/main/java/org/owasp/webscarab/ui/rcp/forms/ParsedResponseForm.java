/**
 *
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

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
            Conversation.PROPERTY_RESPONSE_PROCESSED_CONTENT };

    private boolean readOnly;

    public ParsedResponseForm(FormModel model) {
        super(model, FORM_ID, Conversation.PROPERTY_RESPONSE_HEADERS,
                Conversation.PROPERTY_RESPONSE_PROCESSED_CONTENT);
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

        private Map<Object, Object[]> nodes = new HashMap<Object, Object[]>();

        private ChangeListener changeListener;

        private ValueModel versionVM, statusVM, messageVM, headerVM;

        public ParsedResponseTreeModel(FormModel model) {
            super(model);
            changeListener = new ChangeListener();
            versionVM = model
                    .getValueModel(Conversation.PROPERTY_RESPONSE_VERSION);
            statusVM = model
                    .getValueModel(Conversation.PROPERTY_RESPONSE_STATUS);
            messageVM = model
                    .getValueModel(Conversation.PROPERTY_RESPONSE_MESSAGE);
            headerVM = model
                    .getValueModel(Conversation.PROPERTY_RESPONSE_HEADERS);
            updateNodes();
            versionVM.addValueChangeListener(changeListener);
            statusVM.addValueChangeListener(changeListener);
            messageVM.addValueChangeListener(changeListener);
            headerVM.addValueChangeListener(changeListener);
        }

        private void updateNodes() {
            Map<Object, Object[]> oldNodes = nodes;
            nodes = new HashMap<Object, Object[]>();
            StringBuilder responseLine = new StringBuilder();
            if (versionVM.getValue() != null)
                responseLine.append(versionVM.getValue()).append(" ");
            if (statusVM.getValue() != null)
                responseLine.append(statusVM.getValue()).append(" ");
            if (messageVM.getValue() != null)
                responseLine.append(messageVM.getValue());
            NamedValue[] headers = (NamedValue[]) headerVM.getValue();
            Object[] top;
            if (headers != null) {
                top = new Object[headers.length + 1];
                System.arraycopy(headers, 0, top, 1, headers.length);
            } else {
                top = new Object[1];
            }
            top[0] = responseLine.toString();
            nodes.put(getRoot(), top);
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
            if (!oldChildren[0].equals(newChildren[0])) { // the response line
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
            }
        }

        public void deleteNode(TreePath path, int index, Object node) {
            Object parent = path.getLastPathComponent();
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
            }
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
            Object last = path.getLastPathComponent();
            System.out.println("New value for " + last + " is " + newValue);
            if (last instanceof String) { // response line
                String responseLine = (String) newValue;
                String[] parts = responseLine.split(" ");
                if (parts.length > 0 && parts[0] != null
                        && parts[0].length() != 0) {
                    versionVM.setValue(parts[0]);
                } else {
                    versionVM.setValue(null);
                }
                if (parts.length > 1 && parts[1] != null
                        && parts[1].length() != 0) {
                        statusVM.setValue(parts[1]);
                } else {
                    statusVM.setValue(null);
                }
                if (parts.length > 2 && parts[2] != null
                        && parts[2].length() != 0) {
                    messageVM.setValue(parts[2]);
                } else {
                    messageVM.setValue(null);
                }
                if (parts.length > 3) {
                    System.out.println("Too many parts on the response line: "
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

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

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