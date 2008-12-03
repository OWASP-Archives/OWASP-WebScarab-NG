/**
 * 
 */
package org.owasp.webscarab.util.httpclient.swing;

import javax.swing.JComponent;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;
import org.springframework.binding.form.support.DefaultFormModel;
import org.springframework.richclient.core.DefaultMessage;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author rdawes
 *
 */
public class SwingCredentialsProvider implements CredentialsProvider {

    public SwingCredentialsProvider() {
        
    }
    
    public Credentials getCredentials(AuthScheme scheme, String host, int port,
            boolean proxy) throws CredentialsNotAvailableException {
        final Boolean[] complete = new Boolean[] { Boolean.FALSE };
        final AbstractCredentialsForm form;
        if (scheme instanceof NTLMScheme) {
            form = new NTCredentialsForm();
        } else if (scheme instanceof RFC2617Scheme) {
            form = new UsernamePasswordCredentialsForm();
        } else
            throw new CredentialsNotAvailableException("Unsuported authentication scheme:" + scheme.getSchemeName());
        DialogPage page = new FormBackedDialogPage(form);
        String realm = scheme.getRealm();
        if (realm != null) {
            realm = "\"" + realm + "\"";
        } else {
            realm = host;
        }
        page.setMessage(new DefaultMessage("Enter credentials for " + realm));
        TitledPageApplicationDialog dialog = new TitledPageApplicationDialog(page) {
            @Override
            public boolean onFinish() {
                complete[0] = Boolean.TRUE;
                return true;
            }
        };
        dialog.showDialog();
        Credentials credentials = form.getCredentials(); 
        if (credentials == null || complete[0].equals(Boolean.FALSE))
            throw new CredentialsNotAvailableException();
        return credentials;
    }

    private abstract class AbstractCredentialsForm extends AbstractForm {
        
        protected AbstractCredentialsForm(Credentials credentials, String formId) {
            super(new DefaultFormModel(credentials, false), formId);
        }
        
        public Credentials getCredentials() {
            return (Credentials) getFormModel().getFormObject();
        }
    }
    
    private class UsernamePasswordCredentialsForm extends AbstractCredentialsForm {

        public UsernamePasswordCredentialsForm() {
            super(new UsernamePasswordCredentials(""), "usernamePasswordCredentialsForm");
        }
        
        @Override
        protected JComponent createFormControl() {
            TableFormBuilder formBuilder = new TableFormBuilder(getBindingFactory());
            formBuilder.add("userName");
            formBuilder.row();
            formBuilder.addPasswordField("password");
            formBuilder.row();
            return formBuilder.getForm();
        }
        
    }
    
    private class NTCredentialsForm extends AbstractCredentialsForm {
        
        public NTCredentialsForm() {
            super(new NTCredentials("","","", ""), "ntCredentialsForm");
        }
        
        @Override
        protected JComponent createFormControl() {
            TableFormBuilder formBuilder = new TableFormBuilder(getBindingFactory());
            formBuilder.add("domain");
            formBuilder.row();
            formBuilder.add("userName");
            formBuilder.row();
            formBuilder.addPasswordField("password");
            formBuilder.row();
            return formBuilder.getForm();
        }
        
    }
}
