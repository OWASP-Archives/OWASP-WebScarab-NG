/**
 *
 */
package org.owasp.webscarab.util.httpclient.swing;

import org.owasp.webscarab.util.httpclient.ProxyConfig;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.dialog.ApplicationDialog;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 *
 */
public class UpstreamProxyConfigCommand extends ApplicationWindowAwareCommand {

    private static final String ID = "upstreamProxyConfigCommand";

    private ProxyConfig proxyConfig;

    public UpstreamProxyConfigCommand() {
        super(ID);
    }

    private UpstreamProxyConfigForm createForm() {
        ProxyConfig config = getProxyConfig();
        FormModel model = FormModelHelper.createFormModel(config);
        return new UpstreamProxyConfigForm(model);
    }

    protected void doExecuteCommand() {
        final Form form = createForm();
        DialogPage page = new FormBackedDialogPage(form);
        ApplicationDialog dialog = new TitledPageApplicationDialog(page) {
            protected boolean onFinish() {
                form.commit();
                return true;
            }
        };
        dialog.setCallingCommand(this);
        dialog.showDialog();
    }

    /**
     * @return the proxyConfig
     */
    public ProxyConfig getProxyConfig() {
        return this.proxyConfig;
    }

    /**
     * @param proxyConfig
     *            the proxyConfig to set
     */
    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

}
