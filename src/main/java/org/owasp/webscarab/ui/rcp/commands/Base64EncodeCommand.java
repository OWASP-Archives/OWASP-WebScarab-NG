/**
 * 
 */
package org.owasp.webscarab.ui.rcp.commands;

import javax.swing.text.JTextComponent;

import org.apache.commons.codec.binary.Base64;
import org.springframework.richclient.dialog.Messagable;

/**
 * @author rdawes
 *
 */
public class Base64EncodeCommand extends AbstractTranscoderCommand {

    public Base64EncodeCommand(JTextComponent textComponent) {
        this(textComponent, null);
    }
    
    public Base64EncodeCommand(JTextComponent textComponent, Messagable messagable) {
        super("base64Encode", textComponent, messagable);
    }
    
    public String getCodedText(String text) {
        try {
            return new String(Base64.encodeBase64(text.getBytes("ASCII")), "ASCII");
        } catch (Exception uee) {
            logger.error(uee);
        }
        return null;
    }

}
