/**
 * 
 */
package org.owasp.webscarab.plugins.webservices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLElement;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bushe.swing.event.EventServiceEvent;
import org.dynvocation.lib.xsd4j.XSDParser;
import org.dynvocation.lib.xsd4j.XSDSchema;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.plugins.AbstractPlugin;
import org.owasp.webscarab.util.DOMWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author rdawes
 *
 */
public class Wsdl extends AbstractPlugin {

    private static final String HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    
    public final static String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    
    public final static String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    
    public final static String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    private final Log logger = LogFactory.getLog(this.getClass());
    
    public Definition getWSDL(URI uri) throws IOException, SAXException, WSDLException {
        InputStream is;
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            Conversation conversation = new Conversation();
            conversation.setRequestMethod("GET");
            conversation.setRequestUri(uri);
            conversation.setRequestVersion("HTTP/1.0");
            getHttpService().fetchResponse(conversation, true);
            byte[] content = conversation.getResponseContent();
            if (content != null) {
                is = new ByteArrayInputStream(content);
            } else {
                throw new IOException("No content in response");
            }
        } else if ("file".equals(uri.getScheme())) {
            is = new FileInputStream(new File(uri));
        } else 
            return null;
        return getWSDL(uri, is);
    }
    
    public Definition getWSDL(URI location, InputStream is) throws IOException, SAXException, WSDLException {
        Document xml = parseXML(is);
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        return wsdlReader.readWSDL(location == null ? null : location.toString(), xml);
    }
    
    private Document parseXML(InputStream is) throws IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            InputSource src = new InputSource(is);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(src);
            return document;
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String[] getOperations(Definition definition) {
        Port port = getHttpPort(definition);
        if (port == null)
            return new String[0];
        List<BindingOperation> list = port.getBinding().getBindingOperations();
        String[] operations = new String[list.size()]; 
        Iterator<BindingOperation> it = list.iterator();
        int i=0;
        StringBuilder buff = new StringBuilder();
        while (it.hasNext()) {
            Operation operation = it.next().getOperation();
            Output output = operation.getOutput();
            buff.append(output != null ? output.getName() : "void").append(" ");
            buff.append(operation.getName()).append("(");
            Input input = operation.getInput();
            buff.append(input != null ? input.getName() : "").append(")");
            operations[i++] = buff.toString();
            buff.setLength(0);
        }
        return operations;
    }
    
    @SuppressWarnings("unchecked")
   private BindingOperation getBindingOperation(Definition definition, Port port, String operationSignature) {
        List<BindingOperation> list = port.getBinding().getBindingOperations();
        Iterator<BindingOperation> it = list.iterator();
        StringBuilder buff = new StringBuilder();
        while (it.hasNext()) {
            BindingOperation operation = it.next();
            BindingOutput output = operation.getBindingOutput();
            buff.append(output != null ? output.getName() : "void").append(" ");
            buff.append(operation.getName()).append("(");
            BindingInput input = operation.getBindingInput();
            buff.append(input != null ? input.getName() : "").append(")");
            if (operationSignature.equals(buff.toString()))
                return operation;
            buff.setLength(0);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Port getHttpPort(Definition definition) {
        Map<QName, Service> serviceMap = definition.getServices();
        Iterator<Service> serviceIterator = serviceMap.values().iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            Map<String, Port> portMap = service.getPorts();
            Iterator<Port> portIterator = portMap.values().iterator();
            while (portIterator.hasNext()) {
                Port port = portIterator.next();
                SOAPBinding soapBinding = getSOAPBinding(port);
                if (soapBinding == null)
                    continue;
                if (HTTP_TRANSPORT.equals(soapBinding.getTransportURI()))
                    return port;
            }
        }
        return null;
    }
    
    public Conversation constructRequest(Definition definition, String operationSignature) 
    throws ParserConfigurationException {
        Port port = getHttpPort(definition);
        
        SOAPBinding soapBinding = getSOAPBinding(port);
        if (soapBinding == null) {
            logger.error("No SOAPBinding element found!");
            return null;
        }
        String style = soapBinding.getStyle();
        
        BindingOperation bindingOperation = getBindingOperation(definition, port, operationSignature);
        if (bindingOperation == null)
            return null;

        String use = null;
        String targetNS = null;
        SOAPBody soapBody = getSOAPBody(bindingOperation.getBindingInput());
        if (soapBody != null) {
            use = soapBody.getUse();
            targetNS = soapBody.getNamespaceURI();
        }
        
        Operation operation = bindingOperation.getOperation();
        Conversation c = new Conversation();
        c.setRequestMethod("POST");
        URI targetUri = null;
        try {
            targetUri = new URI(getTargetAddress(port));
        } catch (URISyntaxException urise) {
            logger.error("Invalid Target URI", urise);
            return null;
        }
        c.setRequestUri(targetUri);
        c.setRequestVersion("HTTP/1.0");
        
        c.addRequestHeader(new NamedValue("Accept","application/soap+xml, application/dime, multipart/related, text/*"));
        int portNo = targetUri.getPort();
        if (portNo == -1)
            portNo = "http".equalsIgnoreCase(targetUri.getScheme()) ? 80 : 443;
        c.addRequestHeader(new NamedValue("Host", targetUri.getHost() + ":" + portNo));
        c.addRequestHeader(new NamedValue("Content-Type", "text/xml; charset=utf-8"));
        c.addRequestHeader(new NamedValue("SOAPAction", "\""+ getSoapActionUri(port, operation)+"\""));

        Document doc = createSOAPMessage(definition, operation, targetNS, style, use);
        byte[] body = serializeXML(doc);
        c.setRequestContent(body);

        return c;
    }
    
    private byte[] serializeXML(Document doc) {
        try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter out = new OutputStreamWriter(baos, "UTF-8");
        new DOMWriter().write(out, doc);
        return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Document createSOAPMessage(Definition definition, Operation operation, String targetNS, String style, String use) throws ParserConfigurationException {
        XSDSchema schema = getSchema(definition);
        Map<String, String> nameSpaces = definition.getNamespaces();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document document = dbf.newDocumentBuilder().newDocument();
        Element env = createElement(document, "Envelope", nameSpaces, SOAP_NS);
        String xsdPrefix = getPrefix(nameSpaces, XSD_NS);
        env.setAttribute("xmlns:"+xsdPrefix, XSD_NS);
        String xsiPrefix = getPrefix(nameSpaces, XSI_NS);
        env.setAttribute("xmlns:"+xsiPrefix, XSI_NS);
        Element bodyElem = createElement(env, "Body", nameSpaces, SOAP_NS);
        
        String action = operation.getName();
        Element actionElem = createElement(bodyElem, action, nameSpaces, targetNS);

        if (style.equals("rpc")) {
            Attr enc = createAttr(bodyElem, "encodingStyle", nameSpaces, SOAP_NS);
            enc.setValue("http://schemas.xmlsoap.org/soap/encoding/");
        }
        
        Input input = operation.getInput();
        if (input == null)
            return null;
        List<Part> parts = input.getMessage().getOrderedParts(null);
        Iterator<Part> it = parts.iterator();
        
        while(it.hasNext()) {
            Part part = it.next();
            Element element = createElement(actionElem, part.getName());
            if (style.equals("rpc")) {
                Attr typeAttr = document.createAttributeNS(XSI_NS, "xsi:type");
                QName type = part.getTypeName();
                String prefix = getPrefix(nameSpaces, type.getNamespaceURI());
                typeAttr.setValue(prefix + ":" + type.getLocalPart());
                element.setAttributeNodeNS(typeAttr);
            }
        }
        return document;
    }
    
    private String getPrefix(Map<String, String> namespaces, String namespaceURI) {
        Iterator<Map.Entry<String, String>> it = namespaces.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if (namespaceURI.equals(entry.getValue()))
                return entry.getKey();
        }
        if (SOAP_NS.equals(namespaceURI)) {
            namespaces.put("soap", SOAP_NS);
            return "soap";
        } else if (XSD_NS.equals(namespaceURI)) {
            namespaces.put("xsd", XSD_NS);
            return "xsd";
        } else if (XSI_NS.equals(namespaceURI)) {
            namespaces.put("xsi", namespaceURI);
            return "xsi";
        }
        int i = 0;
        while (namespaces.get("wsns"+i) != null)
            i++;
        namespaces.put("wsns"+i,  namespaceURI);
        return "wsns"+i;
    }
    
    private Element createElement(Node parent, String variableName, Map<String, String> namespaces, String namespaceURI) {
        Document doc;
        if (parent instanceof Document) {
            doc = (Document) parent;
        } else {
            doc = parent.getOwnerDocument();
        }
        String prefix = getPrefix(namespaces, namespaceURI);
        if (prefix == null)
            throw new RuntimeException("No prefix found for " + namespaceURI);
        Element element = doc.createElementNS(namespaceURI, prefix + ":" + variableName);
        parent.appendChild(element);
        return element;
    }
    
    private Element createElement(Node parent, String variableName) {
        Document doc;
        if (parent instanceof Document) {
            doc = (Document) parent;
        } else {
            doc = parent.getOwnerDocument();
        }
        Element element = doc.createElement(variableName);
        parent.appendChild(element);
        return element;
    }
    
    private Attr createAttr(Element element, String attrName, Map<String, String> namespaces, String namespaceURI) {
        String prefix = getPrefix(namespaces, namespaceURI);
        if (prefix == null)
            throw new RuntimeException("No prefix found for " + namespaceURI);
        Document doc;
        if (element instanceof Document) {
            doc = (Document) element;
        } else {
            doc = element.getOwnerDocument();
        }
        Attr attr = doc.createAttributeNS(namespaceURI, prefix + ":" + attrName);
        element.setAttributeNodeNS(attr);
        return attr;
    }

    @SuppressWarnings("unchecked")
    private SOAPBinding getSOAPBinding(Port port) {
        Binding binding = port.getBinding();
        if (binding == null)
            return null;
        ExtensibilityElement bindingElem = findExtensibilityElement(binding.getExtensibilityElements(), "binding");
        if(bindingElem != null && bindingElem instanceof SOAPBinding) {
            return (SOAPBinding)bindingElem;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private String getTargetAddress(Port port) {
        // Find the SOAP target URL
        ExtensibilityElement addrElem = findExtensibilityElement(port.getExtensibilityElements(), "address");
        
        if(addrElem != null && addrElem instanceof SOAPAddress) {
            SOAPAddress soapAddr = (SOAPAddress)addrElem;
            return soapAddr.getLocationURI();
        }
        return null;
    }
    
    private String getSoapActionUri(Port port, Operation operation) {
        Binding binding = port.getBinding();
        BindingOperation bindingOperation = binding.getBindingOperation(operation.getName(), operation.getInput().getName(), operation.getOutput().getName());
        SOAPOperation soapOperation = getSOAPOperation(bindingOperation);
        if (soapOperation == null)
            return null;
        return soapOperation.getSoapActionURI();
    }
    
    @SuppressWarnings("unchecked")
    private SOAPOperation getSOAPOperation(BindingOperation operation) {
        ExtensibilityElement bodyElem = findExtensibilityElement(operation.getExtensibilityElements(), "operation");
        
        if (bodyElem != null && bodyElem instanceof SOAPOperation)
            return (SOAPOperation) bodyElem;
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private SOAPBody getSOAPBody(WSDLElement element) {
        ExtensibilityElement bodyElem = findExtensibilityElement(element.getExtensibilityElements(), "body");
        
        if (bodyElem != null && bodyElem instanceof SOAPBody)
            return (SOAPBody) bodyElem;
        return null;
    }
    
    /**
     * Creates a schema based on the types defined by a WSDL document
     *
     * @param   wsdlDefinition    The WSDL4J instance of a WSDL definition.
     *
     * @return  A schema is returned if the WSDL definition contains
     *          a types element. null is returned otherwise.
     */
    @SuppressWarnings("unchecked")
    protected XSDSchema getSchema(Definition definition) {
        // Get the schema element from the WSDL definition
        Element[] schemaElements = null;
        
        if(definition.getTypes() != null) {
            List<ExtensibilityElement> elements = definition.getTypes().getExtensibilityElements();
            ExtensibilityElement[] schemaExtElems = findExtensibilityElements(elements, "schema");
            
            if(schemaExtElems != null && schemaExtElems.length > 0) {
                schemaElements = new Element[schemaExtElems.length];
                for (int i=0; i<schemaExtElems.length; i++) {
                    if (schemaExtElems[i] instanceof Schema) {
                        schemaElements[i] = ((Schema)schemaExtElems[i]).getElement();
                    } else {
                        schemaElements[i] = null;
                        System.err.println("Looked for schema elements, but got " + schemaExtElems[i].getClass());
                    }
                }
            }
        }
        
        // Add namespaces from the WSDL
        HashMap namespaces = new HashMap(definition.getNamespaces());
        XSDParser parser = new XSDParser();
        parser.declareNamespaces(namespaces);
        if (schemaElements != null)
            for (int i=0; i<schemaElements.length; i++) {
                parser.addSchemaElement(schemaElements[i]);
            }
        XSDSchema schema = parser.parseSchemaElement(null, XSDParser.PARSER_FLAT_INCLUDES);
        return schema;
    }
        
    protected Class<?>[] getSubscribedEvents() {
        return new Class[] { SessionEvent.class };
    }
    
    public void onEvent(EventServiceEvent evt) {
        if (evt instanceof SessionEvent) {
            SessionEvent event = (SessionEvent) evt;
            if (event.getType() == SessionEvent.SESSION_CHANGED) {
                setSession(event.getSession());
            }
        }
    }

    /**
     * Returns the desired ExtensibilityElement if found in the List
     *
     * @param   elements   The list of extensibility elements to search
     * @param   type             The element type to find
     *
     * @return  Returns the first matching element of type found in the list
     */
    private static ExtensibilityElement findExtensibilityElement(List<ExtensibilityElement> elements, String type) {
        if(elements != null) {
            Iterator<ExtensibilityElement> iter = elements.iterator();
            
            while(iter.hasNext()) {
                ExtensibilityElement element = iter.next();
                
                if(element.getElementType().getLocalPart().equalsIgnoreCase(type)) {
                    // Found it
                    return element;
                }
            }
        }
        
        return null;
    }

    /**
     * Returns the desired ExtensibilityElement if found in the List
     *
     * @param   elements   The list of extensibility elements to search
     * @param   type             The element type to find
     *
     * @return  Returns the first matching element of type found in the list
     */
    private static ExtensibilityElement[] findExtensibilityElements(List<ExtensibilityElement> elements, String type) {
        List<ExtensibilityElement> results = new ArrayList<ExtensibilityElement>();
        if(elements != null) {
            Iterator<ExtensibilityElement> iter = elements.iterator();
            
            while(iter.hasNext()) {
                ExtensibilityElement element = iter.next();
                
                if(element.getElementType().getLocalPart().equalsIgnoreCase(type)) {
                    results.add(element);
                }
            }
        }
        return results.toArray(new ExtensibilityElement[0]);
    }
    
}
