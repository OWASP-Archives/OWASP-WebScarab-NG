/**
 * 
 */
package org.owasp.webscarab.ui.rcp.commands;

import java.io.UnsupportedEncodingException;

import javax.swing.text.JTextComponent;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

/**
 * @author rdawes
 *
 */
public class URLDecodeCommand extends AbstractTranscoderCommand {

    public URLDecodeCommand(JTextComponent textComponent) {
        super("urlDecode", textComponent);
    }
    
    public String getCodedText(String text) {
        try {
            return new String(URLCodec.decodeUrl(text.getBytes("ASCII")), "ASCII");
        } catch (DecoderException de) {
            logger.error("Could not decode URLEncoded data", de);
        } catch (UnsupportedEncodingException uee) {
            logger.error("This should never happen", uee);
        }
        return null;
    }
    
}
