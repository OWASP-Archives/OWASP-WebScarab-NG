/**
 * 
 */
package org.owasp.webscarab.ui.rcp.commands;

import javax.swing.text.JTextComponent;

import org.owasp.webscarab.util.HtmlEncoder;

/**
 * @author rdawes
 *
 */
public class HTMLEntityEncodeCommand extends AbstractTranscoderCommand {

    public HTMLEntityEncodeCommand(JTextComponent textComponent) {
        super("htmlEntityEncode", textComponent);
    }
    
    public String getCodedText(String text) {
        return HtmlEncoder.encode(text);
    }
    
}
