/**
 * 
 */
package org.owasp.webscarab.ui.rcp.commands;

import javax.swing.text.JTextComponent;

import org.apache.commons.codec.binary.Base64;

/**
 * @author rdawes
 *
 */
public class Base64DecodeCommand extends AbstractTranscoderCommand {

    public Base64DecodeCommand(JTextComponent textComponent) {
        super("base64Decode", textComponent);
    }
    
    public String getCodedText(String text) {
        try {
            return new String(Base64.decodeBase64(text.getBytes("ASCII")), "ASCII");
        } catch (Exception uee) {
            logger.error(uee);
        }
        return null;
    }

}
