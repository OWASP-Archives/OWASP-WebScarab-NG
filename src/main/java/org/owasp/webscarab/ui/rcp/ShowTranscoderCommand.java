/**
 * 
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.owasp.webscarab.ui.rcp.commands.Base64DecodeCommand;
import org.owasp.webscarab.ui.rcp.commands.Base64EncodeCommand;
import org.owasp.webscarab.ui.rcp.commands.HTMLEntityDecodeCommand;
import org.owasp.webscarab.ui.rcp.commands.HTMLEntityEncodeCommand;
import org.owasp.webscarab.ui.rcp.commands.URLDecodeCommand;
import org.owasp.webscarab.ui.rcp.commands.URLEncodeCommand;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.dialog.ApplicationDialog;
import org.springframework.richclient.dialog.CloseAction;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.util.GuiStandardUtils;

/**
 * @author rdawes
 *
 */
public class ShowTranscoderCommand extends ApplicationWindowAwareCommand {

    private JTextArea textArea;
    
    private FormModel formModel;
    
    private TranscoderForm transcoderForm;
    
    private ApplicationDialog dialog;
    
    public ShowTranscoderCommand() {
        super("showTranscoder");
    }
    
    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
     */
    @Override
    protected void doExecuteCommand() {
        if (dialog == null) {
            formModel = FormModelHelper.createFormModel(new TranscoderContents(), false);
            transcoderForm = new TranscoderForm();
            dialog = new TranscoderDialog();
        }
        dialog.showDialog();
    }

    private class TranscoderDialog extends TitledPageApplicationDialog {
        
        public TranscoderDialog() {
            super(new FormBackedDialogPage(transcoderForm), getParentWindowControl(), CloseAction.HIDE);
            setModal(false);
        }
        @Override
        protected boolean onFinish() {
            return true;
        }
        
        protected String getCancelCommandId() {
            return "closeCommand";
        }
        @Override
        protected Object[] getCommandGroupMembers() {
            return new AbstractCommand[] {getCancelCommand()};
        }

    }
    
    private class TranscoderForm extends AbstractForm {
        
        private JTextArea textArea;
        
        public TranscoderForm() {
            super(formModel, "transcoder");
        }
        
        /* (non-Javadoc)
         * @see org.springframework.richclient.form.AbstractForm#createFormControl()
         */
        @Override
        protected JComponent createFormControl() {
            JPanel panel = getComponentFactory().createPanel(new BorderLayout());
            textArea = (JTextArea) getBindingFactory().createBinding(JTextArea.class, "content").getControl();
            JScrollPane scrollPane = getComponentFactory().createScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600,400));
            panel.add(scrollPane, BorderLayout.CENTER);
            CommandGroup cg = CommandGroup.createCommandGroup(null, getTranscodeCommands());
            JComponent buttonBar = cg.createButtonBar();
            GuiStandardUtils.attachDialogBorder(buttonBar);
            panel.add(buttonBar, BorderLayout.SOUTH);
            return panel;
        }
        
        private Object[] getTranscodeCommands() {
            Base64EncodeCommand b64Encode = new Base64EncodeCommand(textArea);
            Base64DecodeCommand b64Decode = new Base64DecodeCommand(textArea);
            URLEncodeCommand uEncode = new URLEncodeCommand(textArea);
            URLDecodeCommand uDecode = new URLDecodeCommand(textArea);
            HTMLEntityEncodeCommand heEncode = new HTMLEntityEncodeCommand(textArea);
            HTMLEntityDecodeCommand heDecode = new HTMLEntityDecodeCommand(textArea);
            return new AbstractCommand[] {b64Encode, b64Decode, uEncode, uDecode, heEncode, heDecode};
        }
    }
    
    private class TranscoderContents {
        private String content;
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public String getContent() {
            return this.content;
        }
    }
    
}
