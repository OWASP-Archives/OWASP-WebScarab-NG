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
public class HTMLEntityEncodeCommand extends AbstractTranscoderCommand {

    public HTMLEntityEncodeCommand(JTextComponent textComponent) {
        this(textComponent, null);
    }
    
    public HTMLEntityEncodeCommand(JTextComponent textComponent, Messagable messagable) {
        super("htmlEntityEncode", textComponent, messagable);
    }
    
    public String getCodedText(String text) {
        return HtmlEncoder.encode(text);
    }
    
}
