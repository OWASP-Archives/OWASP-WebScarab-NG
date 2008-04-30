/**
 * 
 */
package org.owasp.webscarab.ui.rcp.commands;

import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.core.DefaultMessage;
import org.springframework.richclient.core.Severity;
import org.springframework.richclient.dialog.Messagable;

/**
 * @author rdawes
 *
 */
public abstract class AbstractTranscoderCommand extends ActionCommand implements CaretListener, DocumentListener {

    private JTextComponent textComponent = null;
    
    private Messagable messagable;
    
    public AbstractTranscoderCommand(String id, JTextComponent textComponent, Messagable messagable) {
        super(id);
        this.textComponent = textComponent;
        this.messagable = messagable;
        if (textComponent != null) {
            textComponent.addCaretListener(this);
            textComponent.getDocument().addDocumentListener(this);
        }
        setEnabled(canDecode());
    }
    
    protected JTextComponent getTextComponent() {
        if (this.textComponent != null)
            return textComponent;
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
     */
    @Override
    protected void doExecuteCommand() {
        JTextComponent textComponent = getTextComponent();
        if (textComponent == null)
            return;
        String text = textComponent.getSelectedText();
        if (text == null) {
            textComponent.selectAll();
            text = textComponent.getSelectedText();
        }
        if (text == null)
            return;
        try {
            String newText = getCodedText(text);
            if (newText == null)
                return;
            if (newText.equals(text)) {
                info("No change");
                return;
            }
            textComponent.replaceSelection(text);
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
    }

    protected void info(String message) {
        if (messagable == null) {
            Application.instance().getActiveWindow().getStatusBar().setMessage(message);
        } else { 
            messagable.setMessage(new DefaultMessage(message));
        }
    }
    
    protected void warning(String message) {
        if (messagable == null) {
            Application.instance().getActiveWindow().getStatusBar().setMessage(message);
        } else { 
            messagable.setMessage(new DefaultMessage(message, Severity.WARNING));
        }
    }
    
    private void guard() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setEnabled(canDecode());
            }
        });
    }
    
    abstract public String getCodedText(String text);
    
    private boolean canDecode() {
        JTextComponent tc = getTextComponent();
        if (tc == null || "".equals(tc.getText()))
            return false;
        String text = tc.getSelectedText();
        if (text == null || "".equals(text))
            text = tc.getText();
        if (text == null || "".equals(text))
            return false;
        try {
            return getCodedText(text) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void caretUpdate(CaretEvent e) {
        guard();
    }

    public void changedUpdate(DocumentEvent e) {
        guard();
    }

    public void insertUpdate(DocumentEvent e) {
        guard();
    }

    public void removeUpdate(DocumentEvent e) {
        guard();
    }
    
}
