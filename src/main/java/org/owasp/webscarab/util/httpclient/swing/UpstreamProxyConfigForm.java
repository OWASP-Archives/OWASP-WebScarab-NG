/**
 *
 */
package org.owasp.webscarab.util.httpclient.swing;

import javax.swing.JComponent;

import org.owasp.webscarab.util.httpclient.ProxyConfig;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author rdawes
 *
 */
public class UpstreamProxyConfigForm extends AbstractForm {

    private static final String FORM_ID = "upstreamProxyConfigForm";

    public UpstreamProxyConfigForm(FormModel model) {
        super(model, FORM_ID);
    }

    protected JComponent createFormControl() {
        TableFormBuilder formBuilder = new TableFormBuilder( getBindingFactory() );
        formBuilder.add(ProxyConfig.PROPERTY_HTTP_PROXYHOST);
        formBuilder.add(ProxyConfig.PROPERTY_HTTP_PROXYPORT, "colSpec=2cm");
        formBuilder.row();
        formBuilder.add(ProxyConfig.PROPERTY_HTTPS_PROXYHOST);
        formBuilder.add(ProxyConfig.PROPERTY_HTTPS_PROXYPORT, "colSpec=2cm");
        formBuilder.row();
        formBuilder.addTextArea(ProxyConfig.PROPERTY_NO_PROXY);
        return formBuilder.getForm();
    }

}
