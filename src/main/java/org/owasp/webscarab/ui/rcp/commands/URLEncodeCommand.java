/**
 * 
 */
package org.owasp.webscarab.ui.rcp.commands;

import java.io.UnsupportedEncodingException;

import javax.swing.text.JTextComponent;

import org.apache.commons.codec.net.URLCodec;

/**
 * @author rdawes
 *
 */
public class URLEncodeCommand extends AbstractTranscoderCommand {

    public URLEncodeCommand(JTextComponent textComponent) {
        super("urlEncode", textComponent);
    }
    
    public String getCodedText(String text) {
        try {
            return new String(URLCodec.encodeUrl(null, text.getBytes("ASCII")), "ASCII");
        } catch (UnsupportedEncodingException uee) {
            logger.error("This should never happen", uee);
        }
        return null;
    }
    
}
