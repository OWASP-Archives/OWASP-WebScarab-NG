/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.BorderLayout;
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
public class UrlEncodedJsonForm extends JsonForm {

    private static String FORM_ID = "urlEncodedJsonForm";

    private String jsonFieldName = "";

    private int jsonFieldNum = -1;

    private NamedValue[] values = null;

    private String json = null;
    
    private Listener listener = new Listener();
    
    public UrlEncodedJsonForm(FormModel model, String headerPropertyName,
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
        JLabel fieldLabel = cf.createLabel("jsonField.label");
        JTextField textField = cf.createTextField();
        textField.getDocument().addDocumentListener(listener);
        fieldPanel.add(fieldLabel, BorderLayout.WEST);
        fieldPanel.add(textField, BorderLayout.CENTER);
        panel.add(fieldPanel, BorderLayout.NORTH);
        panel.add(super.createContentFormControl(), BorderLayout.CENTER);
        return panel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.ui.forms.JsonForm#updateContentFormControl()
     */
    @Override
    protected void updateContentFormControl() {
        values = null;
        json = null;
        jsonFieldNum = -1;
        super.updateContentFormControl();
    }

    @Override
    protected void clearContentFormControl() {
        values = null;
        json = null;
        jsonFieldNum = -1;
        super.clearContentFormControl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.ui.forms.AbstractContentForm#getContentAsString()
     */
    @Override
    protected String getContentAsString() throws UnsupportedEncodingException {
        if (json == null) {
            jsonFieldNum = -1;
            String content = super.getContentAsString();
            try {
                values = NamedValue.parse(content, "&", "=");
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        if (jsonFieldName.equals(values[i].getName())) {
                            jsonFieldNum = i;
                            json =  URLDecoder.decode(values[jsonFieldNum].getValue(), "ISO-8859-1");
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
        if (json != null)
            return json;
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.ui.forms.AbstractContentForm#setContent(java.lang.String)
     */
    @Override
    protected void setContent(String content)
            throws UnsupportedEncodingException {
        if (jsonFieldNum != -1) {
            json = content;
            NamedValue nv = new NamedValue(jsonFieldName, URLEncoder.encode(json, "ISO-8859-1"));
            values[jsonFieldNum] = nv;
            super.setContent(NamedValue.join(values, "&", "="));
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
            try {
                Document doc = e.getDocument();
                jsonFieldName = doc.getText(0, doc.getLength());
            } catch (BadLocationException ble) {
                // shouldn't happen
            }
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
