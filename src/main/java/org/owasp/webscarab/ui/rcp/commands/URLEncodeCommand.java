/**
 * 
 */
package org.owasp.webscarab.ui.rcp.commands;

import java.io.UnsupportedEncodingException;

import javax.swing.text.JTextComponent;

import org.apache.commons.codec.net.URLCodec;
import org.springframework.richclient.dialog.Messagable;

/**
 * @author rdawes
 *
 */
public class URLEncodeCommand extends AbstractTranscoderCommand {

    public URLEncodeCommand(JTextComponent textComponent) {
        this(textComponent, null);
    }
    
    public URLEncodeCommand(JTextComponent textComponent, Messagable messagable) {
        super("urlEncode", textComponent, messagable);
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
