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
public class HTMLEntityDecodeCommand extends AbstractTranscoderCommand {

    public HTMLEntityDecodeCommand(JTextComponent textComponent) {
        super("htmlEntityDecode", textComponent);
    }
    
    public String getCodedText(String text) {
        return HtmlEncoder.decode(text);
    }
    
}
