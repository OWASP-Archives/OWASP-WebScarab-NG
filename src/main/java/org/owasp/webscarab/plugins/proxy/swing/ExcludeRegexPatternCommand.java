/**
 *
 */
package org.owasp.webscarab.plugins.proxy.swing;

import javax.swing.JComponent;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.dialog.ApplicationDialog;
import org.springframework.richclient.dialog.CloseAction;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author rdawes
 *
 */
public class ExcludeRegexPatternCommand extends ApplicationWindowAwareCommand {

    private SwingInterceptor swingInterceptor;

    private ApplicationDialog dialog;
    
    public ExcludeRegexPatternCommand() {
        super("excludeRegexPatternCommand");
    }

    /**
     * @param swingInterceptor the swingInterceptor to set
     */
    public void setSwingInterceptor(SwingInterceptor swingInterceptor) {
        this.swingInterceptor = swingInterceptor;
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
     */
    @Override
    protected void doExecuteCommand() {
        if (dialog == null) {
            final FormModel model = FormModelHelper.createFormModel(swingInterceptor, true);
            Form form = new ExcludeRegexForm(model);
            DialogPage page = new FormBackedDialogPage(form);
            dialog = new TitledPageApplicationDialog(page, getParentWindowControl(), CloseAction.HIDE) {
                @Override
                protected boolean onFinish() {
                    model.commit();
                    return true;
                }
            };
        }
        dialog.showDialog();
    }

    private static class ExcludeRegexForm extends AbstractForm {

        public ExcludeRegexForm(FormModel model) {
            super(model, "skipRequestForm");
        }

        @Override
        protected JComponent createFormControl() {
            TableFormBuilder builder = new TableFormBuilder(getBindingFactory());
            builder.add(SwingInterceptor.PROPERTY_SKIP_REQUEST_REGEX);
            builder.row();
            builder.add(SwingInterceptor.PROPERTY_DISCARD_SKIPPED_REQUESTS);
            builder.row();
            return builder.getForm();
        }

    }
}
