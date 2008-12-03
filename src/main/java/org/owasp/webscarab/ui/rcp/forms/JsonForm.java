/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.owasp.webscarab.util.json.JSONComplianceException;
import org.owasp.webscarab.util.json.JSONReader;
import org.owasp.webscarab.util.json.JSONWriter;
import org.springframework.binding.form.FormModel;

/**
 * @author rdawes
 * 
 */
public class JsonForm extends AbstractContentForm {

    private static String FORM_ID = "jsonForm";

    private JXTreeTable treeTable;

    private JsonTreeTableModel model = new JsonTreeTableModel();

    private int compliance;
    
    public JsonForm(FormModel model, String headerPropertyName,
            String contentPropertyName) {
        super(model, FORM_ID, headerPropertyName, contentPropertyName);
    }

    @Override
    protected JComponent createContentFormControl() {
        treeTable = new JXTreeTable(model);
        treeTable.setSearchable(treeTable.new TableSearchable());
        treeTable.setRootVisible(true);
        treeTable.setTreeCellRenderer(new JsonTreeTableCellRenderer());
        updateContentFormControl();
        return getComponentFactory().createScrollPane(treeTable);
    }

    protected void clearContentFormControl() {
        model.setRoot(null);
    }

    protected void updateContentFormControl() {
        try {
            JSONReader reader = new JSONReader(getContentAsString());
            Object json;
            try {
                try {
                    json = reader.parse(JSONReader.STRICT);
                    compliance = JSONReader.STRICT;
                } catch (JSONComplianceException jce) {
                    json = reader.parse(JSONReader.JS_EVAL);
                    compliance = JSONReader.JS_EVAL;
                }
                model.setRoot(constructNodes(json));
            } catch (ParseException pe) {
                json = null;
                compliance = JSONReader.STRICT;
                model.setRoot(null);
            }
            treeTable.expandAll();
        } catch (UnsupportedEncodingException uee) {
            logger.error("Cannot handle the character encoding!", uee);
            clearContentFormControl();
        } catch (IllegalArgumentException iae) {
            logger.error("Invalid JSON data", iae);
            clearContentFormControl();
        }
    }

    public boolean canHandle(String contentType) {
        if (contentType == null)
            return false;
        if ("application/json".equals(contentType))
            return true;
        return false;
    }

    private void updateConversation() {
        try {
            StringWriter sw = new StringWriter();
            BufferedWriter bw = new BufferedWriter(sw);
            JSONWriter writer = new JSONWriter(bw, compliance);
            writer.write(model.getRoot().getUserObject());
            bw.close();
            setContent(sw.getBuffer().toString());
        } catch (UnsupportedEncodingException uee) {
            logger.error("Cannot handle the character encoding!", uee);
            clearContentFormControl();
        } catch (RuntimeException re) {
            logger.error("Invalid JSON data", re);
            clearContentFormControl();
        } catch (IOException ioe) {
            logger.error("Error writing JSON", ioe);
            clearContentFormControl();
        }
    }

    private static Object getKey(Object map, int index) {
        Iterator<?> it = ((Map<?, ?>) map).keySet().iterator();
        for (int i = 0; i < index; i++)
            if (it.hasNext())
                it.next();
        if (it.hasNext())
            return it.next();
        throw new RuntimeException("Should not happen!");
    }

    @SuppressWarnings("unchecked")
	private MutableTreeTableNode constructNodes(Object obj) {
        DefaultMutableTreeTableNode node;
        if (obj instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) obj;
            node = new DefaultMutableTreeTableNode(map, true);
            Iterator<Object> it = map.keySet().iterator();
            while (it.hasNext())
                node.add(constructNodes(map.get(it.next())));
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            node = new DefaultMutableTreeTableNode(list, true);
            Iterator<Object> it = list.iterator();
            while (it.hasNext())
                node.add(constructNodes(it.next()));
        } else {
            node = new DefaultMutableTreeTableNode(obj);
        }
        return node;
    }

    private class JsonTreeTableModel extends DefaultTreeTableModel {

        private final String[] columnNames = new String[] { "Node", "Type",
                "Value" };

        /*
         * (non-Javadoc)
         * 
         * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount() {
            return 3;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#getValueAt(java.lang.Object,
         *      int)
         */
        @Override
        public Object getValueAt(Object node, int column) {
            if (node instanceof DefaultMutableTreeTableNode)
                node = ((DefaultMutableTreeTableNode) node).getUserObject();

            if (column == 0) {
                return this;
            } else if (column == 1) {
                StringBuffer value = new StringBuffer();
                if (node == null) {
                    value.append("Null");
                } else if (node instanceof Map) {
                    value.append("Object (").append(((Map<?, ?>) node).size())
                            .append(" fields)");
                } else if (node instanceof Collection) {
                    value.append("Array (").append(
                            ((Collection<?>) node).size()).append(" entries)");
                } else if (node instanceof Number) {
                    value.append("Number");
                } else if (node instanceof Boolean) {
                    value.append("Boolean");
                } else if (node instanceof String) {
                    value.append("String");
                } else
                    value.append("Unknown! '" + node + "'");
                return value.toString();
            } else if (column == 2) {
                if (node instanceof Map) {
                    return null; // leave it empty
                } else if (node instanceof Collection) {
                    return null; // leave it empty
                } else if (node == null) {
                    return "null";
                } else {
                    return node;
                }
            }
            throw new RuntimeException("Should never happen! column = "
                    + column);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#isCellEditable(java.lang.Object,
         *      int)
         */
        @Override
        public boolean isCellEditable(Object node, int column) {
            if (node instanceof DefaultMutableTreeTableNode)
                node = ((DefaultMutableTreeTableNode) node).getUserObject();

            return !isReadOnly()
                    && column == 2
                    && ((node instanceof String) || (node instanceof Number) || (node instanceof Boolean));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jdesktop.swingx.treetable.DefaultTreeTableModel#setValueAt(java.lang.Object,
         *      java.lang.Object, int)
         */
        @SuppressWarnings("unchecked")
		@Override
        public void setValueAt(Object value, Object node, int column) {
            DefaultMutableTreeTableNode ttNode = null;
            if (node instanceof DefaultMutableTreeTableNode)
                ttNode = ((DefaultMutableTreeTableNode) node);

            Object oldValue = ttNode.getUserObject();
            Object newValue = null;
            if (oldValue instanceof Number && value instanceof String) {
                String s = (String) value;
                boolean fp = s.contains(".");
                if (fp) {
                    newValue = new BigDecimal(s);
                } else {
                    newValue = new BigInteger(s);
                }
            } else if (oldValue instanceof String && value instanceof String) {
                newValue = value;
            } else if (oldValue instanceof Boolean && value instanceof String) {
                newValue = Boolean.parseBoolean((String) value);
            } else {
                logger.error("Don't know how to deal with old "
                        + oldValue.getClass().getName() + " and new "
                        + value.getClass());
            }
            ttNode.setUserObject(newValue);

            Object parent = null;
            if (ttNode.getParent() != null)
                parent = ttNode.getParent().getUserObject();
            if (parent instanceof Map) {
                Map map = (Map) parent;
                Object key = getKey(map, ttNode.getParent().getIndex(ttNode));
                map.put(key, newValue);
            } else if (parent instanceof List) {
                List<Object> list = (List<Object>) parent;
                list.set(ttNode.getParent().getIndex(ttNode), newValue);
            } else {
                logger.error("Don't know how to set a new value in a "
                        + parent.getClass());
            }
            updateConversation();
            modelSupport.firePathChanged(new TreePath(getPathToRoot(ttNode)));
        }

    }

    public class JsonTreeTableCellRenderer extends DefaultTreeCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		boolean once = false;

        Color elementColor = new Color(0, 0, 128);

        Color attributeColor = new Color(0, 128, 0);

        public JsonTreeTableCellRenderer() {
            setOpenIcon(null);
            setClosedIcon(null);
            setLeafIcon(null);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object node,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            DefaultMutableTreeTableNode ttNode = (DefaultMutableTreeTableNode) node;
            Object parent = null;
            if (ttNode.getParent() != null)
                parent = ttNode.getParent().getUserObject();

            StringBuffer value = new StringBuffer();
            if (parent instanceof Map) {
                value.append("\""
                        + getKey(parent, ttNode.getParent().getIndex(ttNode))
                        + "\"");
            } else if (parent instanceof Collection) {
                value.append("Index : " + ttNode.getParent().getIndex(ttNode));
            }
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);
            return this;
        }
    }

}
