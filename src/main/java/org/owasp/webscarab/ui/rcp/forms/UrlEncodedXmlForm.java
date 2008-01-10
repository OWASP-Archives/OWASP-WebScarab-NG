/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.factory.ComponentFactory;

/**
 * @author rdawes
 * 
 */
public class UrlEncodedXmlForm extends XmlForm {

    private static String xmlFieldName = "";

    private int xmlFieldNum = -1;

    private NamedValue[] values = null;

    private String xml = null;
    
    private Document document = null;
    
    private Listener listener = new Listener();
    
    public UrlEncodedXmlForm(FormModel model, String headerPropertyName,
            String contentPropertyName) {
        super(model, headerPropertyName, contentPropertyName);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.ui.forms.JsonForm#createContentFormControl()
     */
    @Override
    protected JComponent createContentFormControl() {
        ComponentFactory cf = getComponentFactory();
        JPanel panel = cf.createPanel(new BorderLayout());
        JPanel fieldPanel = cf.createPanel(new BorderLayout());
        JLabel fieldLabel = cf.createLabel("xmlField.label");
        JTextField textField = cf.createTextField();
        textField.setText(xmlFieldName);
        document = textField.getDocument();
        document.addDocumentListener(listener);
        fieldPanel.add(fieldLabel, BorderLayout.WEST);
        fieldPanel.add(textField, BorderLayout.CENTER);
        panel.add(fieldPanel, BorderLayout.NORTH);
        panel.add(super.createContentFormControl(), BorderLayout.CENTER);
        return panel;
    }

    private String getXmlFieldName() {
        try {
            String fieldName = document.getText(0, document.getLength());
            xmlFieldName = fieldName;
            return fieldName;
        } catch (BadLocationException ble) {
            // shouldn't happen
        }
        return "";
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.ui.forms.JsonForm#updateContentFormControl()
     */
    @Override
    protected void updateContentFormControl() {
        values = null;
        xml = null;
        xmlFieldNum = -1;
        super.updateContentFormControl();
    }

    @Override
    protected void clearContentFormControl() {
        values = null;
        xml = null;
        xmlFieldNum = -1;
        super.clearContentFormControl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.ui.forms.AbstractContentForm#getContentAsString()
     */
    @Override
    protected InputStream getContentAsStream() {
        try {
            if (xml == null) {
                xmlFieldNum = -1;
                String content = super.getContentAsString();
                try {
                    values = NamedValue.parse(content, "&", "=");
                    if (values != null) {
                        for (int i = 0; i < values.length; i++) {
                            if (getXmlFieldName().equals(values[i].getName())) {
                                xmlFieldNum = i;
                                xml =  URLDecoder.decode(values[xmlFieldNum].getValue(), "ISO-8859-1");
                                break;
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException aioob) {
                    System.out.println("Content was " + content);
                    aioob.printStackTrace();
                    // Happens if the content doesn't contain "="
                    // do nothing?
                }
            }
        } catch (UnsupportedEncodingException uee) {
            xml = null;
            xmlFieldNum = -1;
        }
        if (xml != null)
            return new ByteArrayInputStream(xml.getBytes());
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    protected void setContent(byte[] content) {
        if (xmlFieldNum != -1) {
            try {
                xml = new String(content);
                NamedValue nv = new NamedValue(getXmlFieldName(), URLEncoder.encode(xml, "ISO-8859-1"));
                values[xmlFieldNum] = nv;
                super.setContent(NamedValue.join(values, "&", "=").getBytes());
            } catch (UnsupportedEncodingException uee) {
                updateContentFormControl();
            }
        }
    }

    public boolean canHandle(String contentType) {
        if (contentType == null)
            return false;
        if ("application/x-www-form-urlencoded".equals(contentType))
            return true;
        return false;
    }

    private class Listener implements DocumentListener {

        private void updateField(DocumentEvent e) {
            updateContentFormControl();
        }
        public void changedUpdate(DocumentEvent e) {
            updateField(e);
        }
        public void insertUpdate(DocumentEvent e) {
            updateField(e);
        }
        public void removeUpdate(DocumentEvent e) {
            updateField(e);
        }
        
    }
}
