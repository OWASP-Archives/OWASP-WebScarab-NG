/**
 *
 */
package org.owasp.webscarab.plugins.proxy.swing;

import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.dialog.ConfirmationDialog;
import org.springframework.richclient.dialog.InputApplicationDialog;

/**
 * @author rdawes
 *
 */
public class ExcludeRegexPatternCommand extends ApplicationWindowAwareCommand {

    private SwingInterceptor swingInterceptor;

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
        InputApplicationDialog dialog = new InputApplicationDialog(swingInterceptor, "skipRequestRegex", true);
        dialog.setCallingCommand(this);
        dialog.setParentComponent(getApplicationWindow().getControl());
        dialog.setInputLabelMessage("skipRequestRegex.label");
        dialog.setModal(true);
        dialog.showDialog();
    }


}
