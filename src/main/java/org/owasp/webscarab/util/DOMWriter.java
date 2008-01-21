package org.owasp.webscarab.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author rdawes
 */
public class DOMWriter {
    
    /** Creates a new instance of DOMWriter */
    public DOMWriter() {
    }
    
    private void write_xslt(Writer writer, Document document) {
        try {
            StreamSource xslt = new StreamSource(getClass().getClassLoader().getResourceAsStream("org/owasp/webscarab/util/indent.xsl"));
            Transformer transformer = TransformerFactory.newInstance().newTransformer(xslt);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    
            Result result = new StreamResult(writer);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    

    public void write(Writer writer, Document document) throws IOException {
        if (true == false) {
            write_xslt(writer, document);
            return;
        }
        BufferedWriter bw = new BufferedWriter(writer);
        bw.write("<?xml version='1.0' encoding='UTF-8'?>\n");
        NodeList children = document.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            write(bw, null, children.item(i), 0);
        }
        bw.flush();
        bw.close();
    }
    
    private void write(Writer writer, NSStack nsstack, Node node, int depth) throws IOException {
        nsstack = new NSStack(nsstack);
        if (node.getPrefix() != null) {
            String nsuri = nsstack.getNamespace(node.getPrefix());
            if (nsuri == null) {
                nsstack.addNamespace(node.getPrefix(), node.getNamespaceURI());
            }
        }
        writer.write(pad(depth));
        writer.write("<"+node.getNodeName());
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            writer.write(" ");
//            writer.write("\n");
            for (int i=0; i<attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                if (attr.getPrefix() != null) {
                    String nsuri = nsstack.getNamespace(attr.getPrefix());
                    if (nsuri == null) {
                        nsstack.addNamespace(attr.getPrefix(), attr.getNamespaceURI());
                    }
                } else if (attr.getNodeName().startsWith("xmlns:")) {
                    String prefix = attr.getNodeName().substring(6);
                    String nsuri = attr.getNodeValue();
                    nsstack.addNamespace(prefix, nsuri);
                }
                String value = attr.getValue();
//                writer.write(pad(depth+1));
                writer.write(attr.getName() + "='" + value + "'");
                if (i<attrs.getLength())
                    writer.write(" ");
//                writer.write("\n");
                if (value != null && value.indexOf(":")>-1 && value.indexOf("://")==-1) {
                    String prefix = value.substring(0, value.indexOf(":"));
                    String nsuri = nsstack.getNamespace(prefix);
                    if (nsuri == null) {
                        System.err.println("WARNING: Use of undeclared namespace prefix : " + prefix);
                    }
                }
            }
//            writer.write(pad(depth));
        }
        Map namespaces = nsstack.getNamespaces();
        if (namespaces != null) {
            Iterator nsit = namespaces.keySet().iterator();
            while (nsit.hasNext()) {
                String prefix = (String) nsit.next();
                String nsuri = (String) namespaces.get(prefix);
                Attr attr = (Attr) node.getAttributes().getNamedItem("xmlns:"+prefix);
                if (attr == null) {
//                    writer.write(pad(depth+1));
                    writer.write("xmlns:"+prefix+"='"+nsuri + "'");
//                    writer.write("\n");
                }
            }
//            writer.write(pad(depth));
        }
        writer.write(">");
        NodeList children = node.getChildNodes();
        if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
            Node text = children.item(0);
            writer.write(text.getNodeValue());
        } else if (children.getLength()>0) {
            writer.write("\n");
            for (int i=0; i<children.getLength(); i++) {
                write(writer, nsstack, children.item(i), depth+1);
            }
            writer.write(pad(depth));
        }
        writer.write("</"+node.getNodeName()+">\n");
    }
    
    private String pad(int depth) {
        StringBuffer buff = new StringBuffer();
        for (int i=0; i<depth; i++) {
            buff.append("  ");
        }
        return buff.toString();
    }
    
    private class NSStack {
        
        private Map namespaces = null;
        private NSStack parent = null;
        
        public NSStack() {
        }
        
        public NSStack(NSStack parent) {
            this.parent = parent;
        }
        
        public Map getNamespaces() {
            return namespaces;
        }
        
        public void addNamespace(String prefix, String nsuri) {
            if (namespaces == null)
                namespaces = new HashMap();
            namespaces.put(prefix, nsuri);
        }
        
        public String getNamespace(String prefix) {
            if (namespaces != null) {
                String result = (String) namespaces.get(prefix);
                if (result != null) return result;
            }
            if (parent != null)
                return parent.getNamespace(prefix);
            return null;
        }
        
    }
}
