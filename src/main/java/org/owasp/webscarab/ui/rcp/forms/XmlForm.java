/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.Color;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.owasp.webscarab.util.DOMWriter;
import org.springframework.binding.form.FormModel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author rdawes
 * 
 */
public class XmlForm extends AbstractContentForm {

    private static String FORM_ID = "xmlForm";

    private static final EntityResolver nullEntityResolver = new NullEntityResolver();
    
    private JXTreeTable treeTable;

    private Color defaultBackground, error = Color.PINK;
    
    private XmlTreeTableModel model = new XmlTreeTableModel();

    private Document document = null;

    public XmlForm(FormModel model, String headerPropertyName,
            String contentPropertyName) {
        super(model, FORM_ID, headerPropertyName, contentPropertyName);
    }

    @Override
    protected JComponent createContentFormControl() {
        treeTable = new JXTreeTable(model);
        defaultBackground = treeTable.getBackground();
        treeTable.setSearchable(treeTable.new TableSearchable());
        treeTable.setRootVisible(true);
        treeTable.setTreeCellRenderer(new XMLTreeTableCellRenderer());
        updateContentFormControl();
        return getComponentFactory().createScrollPane(treeTable);
    }

    protected void clearContentFormControl() {
        document = null;
        model.fireRootChanged();
        treeTable.setBackground(defaultBackground);
    }

    protected void updateContentFormControl() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                    .newInstance();
            builderFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.setEntityResolver(nullEntityResolver);
            document = builder.parse(getContentAsStream());
            pruneEmptyTextElements(document);
            model.fireRootChanged();
            treeTable.expandAll();
            treeTable.setBackground(defaultBackground);
        } catch (ParserConfigurationException pce) {
            logger.error("Error configuring XML parser", pce);
            treeTable.setBackground(error);
        } catch (SAXException se) {
            logger.info("Error parsing XML");
            treeTable.setBackground(error);
        } catch (IOException ioe) {
            // should never happen
            treeTable.setBackground(error);
        }
    }

    public boolean canHandle(String contentType) {
        if (contentType == null)
            return false;
        if ("text/xml".equalsIgnoreCase(contentType))
            return true;
        return false;
    }

    private void updateConversation() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");
            new DOMWriter().write(writer, document);
            writer.close();
            setContent(baos.toByteArray());
        } catch (IOException ioe) {
            logger.error("Error writing XML back to the conversation", ioe);
            updateContentFormControl();
        }
    }
    
    private void pruneEmptyTextElements(Node node) {
        NodeList children = node.getChildNodes();
        for (int i=children.getLength() - 1; i>=0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE ) {
                String text = child.getTextContent();
                if (isWhitespace(text))
                    node.removeChild(child);
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                pruneEmptyTextElements(child);
            } else if (child.getNodeType() == Node.DOCUMENT_NODE) {
                pruneEmptyTextElements(child);
            }
        }
    }
    
    private boolean isWhitespace(String text) {
        boolean whitespace = true;
        if (text != null) {
            for (int i=0; i<text.length(); i++)
                if (!Character.isWhitespace(text.charAt(i))) {
                    whitespace = false;
                    break;
                }
        }
        return whitespace;
    }
    
    private class XmlTreeTableModel extends AbstractTreeTableModel {

        /*
         * (non-Javadoc)
         * 
         * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#getRoot()
         */
        @Override
        public Object getRoot() {
            if (document == null)
                return null;
            return document.getDocumentElement();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jdesktop.swingx.treetable.TreeTableModel#getColumnCount()
         */
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? "Node" : "Value";
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? TreeTableModel.class : String.class;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jdesktop.swingx.treetable.TreeTableModel#getValueAt(java.lang.Object,
         *      int)
         */
        public Object getValueAt(Object node, int column) {
            if (column == 0)
                return node;
            else {
                Node n = (Node) node;
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList children = n.getChildNodes();
                    int childCount = children != null ? children.getLength()
                            : 0;
                    if (childCount == 1
                            && children.item(0).getNodeType() == Node.TEXT_NODE)
                        return children.item(0).getNodeValue();
                }
                return n.getNodeValue();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
         */
        public Object getChild(Object parent, int index) {
            Node node = (Node) parent;
            NamedNodeMap attrs = node.getAttributes();
            int attrCount = attrs != null ? attrs.getLength() : 0;
            if (index < attrCount)
                return attrs.item(index);
            NodeList children = node.getChildNodes();
            return children.item(index - attrCount);
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
         */
        public int getChildCount(Object parent) {
            Node node = (Node) parent;
            NamedNodeMap attrs = node.getAttributes();
            int attrCount = attrs != null ? attrs.getLength() : 0;
            NodeList children = node.getChildNodes();
            int childCount = children != null ? children.getLength() : 0;
            if (childCount == 1
                    && children.item(0).getNodeType() == Node.TEXT_NODE)
                return attrCount;
            else
                return attrCount + childCount;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
         *      java.lang.Object)
         */
        public int getIndexOfChild(Object parent, Object child) {
            Node node = (Node) parent;
            Node childNode = (Node) child;

            NamedNodeMap attrs = node.getAttributes();
            int attrCount = attrs != null ? attrs.getLength() : 0;
            if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                for (int i = 0; i < attrCount; i++) {
                    if (attrs.item(i) == child)
                        return i;
                }
            } else {
                NodeList children = node.getChildNodes();
                int childCount = children != null ? children.getLength() : 0;
                for (int i = 0; i < childCount; i++) {
                    if (children.item(i) == child)
                        return attrCount + i;
                }
            }
            throw new RuntimeException("this should never happen!");
        }

        public void fireRootChanged() {
            modelSupport.fireNewRoot();
        }

        /* (non-Javadoc)
         * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#isCellEditable(java.lang.Object, int)
         */
        @Override
        public boolean isCellEditable(Object node, int column) {
            return !isReadOnly() && column == 1;
        }

        /* (non-Javadoc)
         * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
         */
        @Override
        public void setValueAt(Object value, Object node, int column) {
            Node n = (Node) node;
            n.setTextContent((String) value);
            updateConversation();
        }

    }

    public class XMLTreeTableCellRenderer extends DefaultTreeCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Color elementColor = new Color(0, 0, 128);

        Color attributeColor = new Color(0, 128, 0);

        public XMLTreeTableCellRenderer() {
            setOpenIcon(null);
            setClosedIcon(null);
            setLeafIcon(null);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            Node node = (Node) value;
            switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                value = '<' + node.getNodeName() + '>';
                break;
            case Node.ATTRIBUTE_NODE:
                value = '@' + node.getNodeName();
                break;
            case Node.TEXT_NODE:
                value = "# text";
                break;
            case Node.COMMENT_NODE:
                value = "# comment";
                break;
            case Node.DOCUMENT_TYPE_NODE:
                value = "# doctype";
                break;
            default:
                value = node.getNodeName();
            }
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);
            if (!selected) {
                switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    setForeground(elementColor);
                    break;
                case Node.ATTRIBUTE_NODE:
                    setForeground(attributeColor);
                    break;
                }
            }
            return this;
        }
    }

    private static class NullEntityResolver implements EntityResolver {
        private InputStream is = new ByteArrayInputStream(new byte[0]);
        public InputSource resolveEntity(String publicId, String systemId){ 
            return new InputSource(is); 
        } 
    }
}
