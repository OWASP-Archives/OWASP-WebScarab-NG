/**
 * 
 */
package org.owasp.webscarab.ui.rcp.commands;

import javax.swing.text.JTextComponent;

import org.owasp.webscarab.util.HtmlEncoder;
import org.springframework.richclient.dialog.Messagable;

/**
 * @author rdawes
 *
 */
public class HTMLEntityDecodeCommand extends AbstractTranscoderCommand {

    public HTMLEntityDecodeCommand(JTextComponent textComponent) {
        this(textComponent, null);
    }
    
    public HTMLEntityDecodeCommand(JTextComponent textComponent, Messagable messagable) {
        super("htmlEntityDecode", textComponent, messagable);
    }
    
    public String getCodedText(String text) {
        return HtmlEncoder.decode(text);
    }
    
}
