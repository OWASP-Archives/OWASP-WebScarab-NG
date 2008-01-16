/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.io.IOException;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;

/**
 * @author rdawes
 *
 */
public interface ProxyInterceptor {

	public void editRequest(Conversation conversation, Annotation annotation) throws IOException;
	
	public void editResponse(Conversation conversation, Annotation annotation) throws IOException;
	
	public boolean shouldRecordConversation(Conversation conversation);
	
}
