/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.io.IOException;

import org.owasp.webscarab.Annotation;
import org.owasp.webscarab.Conversation;

/**
 * @author rdawes
 *
 */
public interface ProxyInterceptor {

	public void editRequest(Conversation conversation, Annotation annotation) throws IOException;
	
	public void editResponse(Conversation conversation, Annotation annotation) throws IOException;
	
}
